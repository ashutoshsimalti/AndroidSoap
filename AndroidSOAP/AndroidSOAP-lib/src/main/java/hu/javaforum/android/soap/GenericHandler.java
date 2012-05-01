/**
 * CC-LGPL 2.1
 * http://creativecommons.org/licenses/LGPL/2.1/
 */
package hu.javaforum.android.soap;

import hu.javaforum.commons.ReflectionUtil;
import hu.javaforum.logger.Logger;
import hu.javaforum.logger.PerfLogger;
import hu.javaforum.logger.PerfTracer;
import hu.javaforum.logger.Tracer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Builds the tree of the objects from the SOAP XML.
 *
 * Changelog:
 * ANDROIDSOAP-6 - 2011-01-07
 * ANDROIDSOAP-5 - 2011-01-07
 * ANDROIDSOAP-3 - 2011-01-07
 * ANDROIDSOAP-1 - 2011-01-06
 *
 * @author Gábor Auth <gabor.auth@javaforum.hu>
 * @author Chris Wolf cw10025 gmail com
 * @author sabo
 */
public class GenericHandler extends ParserHelper
{

  /**
   * The LOGGER instance.
   */
  private static final Logger LOGGER = new Logger(GenericHandler.class.getSimpleName());
  /**
   * The TRACER instance.
   */
  private static final Tracer TRACER = new Tracer(GenericHandler.class.getSimpleName());
  /**
   * The result root object.
   */
  private final Object result;
  /**
   * The path in the SOAP XML.
   */
  private final List<String> xmlPath = new ArrayList<String>();
  /**
   * The objects in the XML.
   */
  private final List<Object> objectPath = new ArrayList<Object>();
  /**
   * The collection objects in the XML.
   */
  private final List<Object> collectionPath = new ArrayList<Object>();
  /**
   * The ArrayFieldInfo list.
   */
  private final Stack<ArrayFieldInfo> arrayFieldData = new Stack<ArrayFieldInfo>();
  /**
   * The wrapped result.
   */
  private Object wrappedResult;

  /**
   * Constructor used for case where we have wrapped result and field name
   * does not match result. This is a common use-case for
   * document-literal/wrapped web services.
   *
   * @param resultClass The class of result object
   * @throws IllegalAccessException IllegalAccessException
   * @throws InstantiationException InstantiationException
   */
  public GenericHandler(final Class resultClass) throws InstantiationException, IllegalAccessException
  {
    PerfTracer tracer = new PerfTracer(TRACER);
    PerfLogger logger = new PerfLogger(LOGGER);
    tracer.entry();

    try
    {
      this.result = resultClass.newInstance();
      Object wrappedResultObject = null;
      try
      {
        Class wrappedResultClass = ReflectionUtil.getField(resultClass, "return").getType();
        if (wrappedResultClass != null)
        {
          wrappedResultObject = wrappedResultClass.newInstance();
          ReflectionUtil.invokeSetter(result, "return", wrappedResultObject);
        }
      } catch (NoSuchFieldException except)
      {
        logger.warn(except.toString());
      }
      this.wrappedResult = wrappedResultObject;
    } finally
    {
      tracer.exit();
    }
  }

  /**
   * Gets the result object.
   *
   * @return The object
   */
  public final Object getObject()
  {
    return this.result;
  }

  /**
   * Start element hook.
   *
   * @param originalName The name of the tag
   */
  public final void startElement(final String originalName)
  {
    PerfTracer tracer = new PerfTracer(TRACER);
    PerfLogger logger = new PerfLogger(LOGGER);
    tracer.entry();

    try
    {
      this.clearContent();

      StringBuilder sb = new StringBuilder(originalName);
      sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
      String name = sb.toString();

      if (name.indexOf("env:") == 0)
      {
        logger.debug("Skipping %1$s", name);
        return;
      }

      if (this.xmlPath.isEmpty())
      {
        this.objectPath.add(result);
        this.collectionPath.add(null);
      } else if (this.xmlPath.size() == 1 && this.wrappedResult != null)
      {
        this.objectPath.add(this.wrappedResult);
        this.collectionPath.add(null);

      } else
      {
        /**
         * Gets the last object...
         */
        logger.debug("XML tag name: %1$s", name);
        Object object = this.objectPath.get(this.objectPath.size() - 1);
        Class fieldClass = null;
        try
        {
          fieldClass = ReflectionUtil.getField(object.getClass(), name).getType();
        } catch (NoSuchFieldException ex)
        {
          logger.warn(ex.toString());
        }

        /**
         * ...detect collection types
         */
        if (fieldClass != null && (fieldClass.equals(List.class) || fieldClass.isArray()))
        {
          /**
           * Gets the generic class
           */
          Class fieldGenericClass = ReflectionUtil.getFieldGenericClass(object.getClass(), name);
          List listObject;
          if (fieldClass.equals(List.class))
          {
            listObject = (List) ReflectionUtil.invokeGetter(object, name);
            this.collectionPath.add(listObject);
          } else
          {
            ArrayFieldInfo afi = null;
            if (this.arrayFieldData.size() > 0)
            {
              afi = this.arrayFieldData.peek();
            }
            if (afi == null)
            {
              afi = new ArrayFieldInfo(name, this.xmlPath.size());
              this.arrayFieldData.push(afi);
            }
            listObject = afi.getArrayData();
            this.collectionPath.add(null);
          }

          /**
           * Creates a new instance, and put it into the objects collection
           */
          try
          {
            Object fieldObject = fieldGenericClass.newInstance();
            this.objectPath.add(fieldObject);
            listObject.add(fieldObject);
          } catch (InstantiationException except)
          {
            logger.error(except.toString(), except);
          } catch (IllegalAccessException except)
          {
            logger.error(except.toString(), except);
          }
        } else
        {
          /**
           * Creates a new simple instance, and put it into the objects collection
           */
          try
          {
            if (fieldClass.isPrimitive() || fieldClass.isArray())
            {
              this.objectPath.add(fieldClass);
              this.collectionPath.add(null);
            } else if (fieldClass.getName().equals("java.math.BigDecimal"))
            {
              Object fieldObject = fieldClass.getDeclaredConstructor(int.class).newInstance(0);
              this.objectPath.add(fieldObject);
              this.collectionPath.add(null);
              Object parentObject = this.objectPath.get(this.objectPath.size() - 2);
              ReflectionUtil.invokeSetter(parentObject, name, fieldObject);
            } else if (fieldClass.isEnum())
            {
              this.objectPath.add(fieldClass);
              this.collectionPath.add(null);
            } else
            {
              Object fieldObject = fieldClass.newInstance();
              this.objectPath.add(fieldObject);
              this.collectionPath.add(null);

              Object parentObject = this.objectPath.get(this.objectPath.size() - 2);
              ReflectionUtil.invokeSetter(parentObject, name, fieldObject);
            }
          } catch (Exception except)
          {
            logger.error(except.toString(), except);
          }
        }
      }

      this.xmlPath.add(name);

      logger.debug("startElement.xmlPath: %1$s ", this.xmlPath.toString());
      logger.debug("startElement.objectPath: %1$s", this.objectPath.toString());
      logger.debug("startElement.collectionPath: %1$s", this.collectionPath.toString());
    } finally
    {
      tracer.exit();
    }
  }

  /**
   * End element hook.
   *
   * @param originalName The name of the tag
   */
  public final void endElement(final String originalName)
  {
    PerfTracer tracer = new PerfTracer(TRACER);
    tracer.entry();

    try
    {
      PerfLogger logger = new PerfLogger(LOGGER);

      if (originalName.indexOf("env:") == 0)
      {
        return;
      }

      StringBuilder sb = new StringBuilder(originalName);
      sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
      String name = sb.toString();

      this.xmlPath.remove(this.xmlPath.size() - 1);
      /**
       * If the parse depth drops back to where we first encountered array elements,
       * gather the assembled array elements and call the parent object's array setter
       * TODO: now handling array fields CJW
       */
      if (this.arrayFieldData != null && this.arrayFieldData.size() > 0)
      {
        ArrayFieldInfo afi = this.arrayFieldData.peek();
        if (this.xmlPath.size() < afi.getArrayFieldDepth())
        {
          this.arrayFieldData.pop();
          Object object = this.objectPath.get(this.objectPath.size() - 1);
          logger.debug("invokeSetter(" + object + ", " + afi.getFieldName() + ", " + afi.getArrayData() + ")");
          ReflectionUtil.invokeSetter(object, afi.getFieldName(), afi.getArrayData());
        }
      }

      if (this.xmlPath.isEmpty())
      {
        this.objectPath.remove(result);
        this.collectionPath.remove(this.collectionPath.size() - 1);
      } else if (this.xmlPath.size() == 1 && this.wrappedResult != null)
      {
        this.objectPath.remove(this.wrappedResult);
        this.collectionPath.remove(this.collectionPath.size() - 1);
      } else
      {
        Object object = this.objectPath.get(this.objectPath.size() - 2);
        Object field = this.objectPath.get(this.objectPath.size() - 1);

        Class fieldClass = null;
        if (field instanceof Class)
        {
          fieldClass = (Class) field;
        } else
        {
          try
          {
            fieldClass = ReflectionUtil.getField(object.getClass(), name).getType();
          } catch (NoSuchFieldException ex)
          {
            logger.warn(ex.toString());
          }
        }

        /**
         * Save the content, if the field is String - data conversions occur in invokeSetter
         */
        if (fieldClass != null && !fieldClass.isArray())
        {
          String content = getContent();
          logger.debug("invokeSetter(%1$s, %2$s, %3$s", object, name, content);
          ReflectionUtil.invokeSetter(object, name, content);
        }

        this.objectPath.remove(this.objectPath.size() - 1);
        this.collectionPath.remove(this.collectionPath.size() - 1);
      }

      logger.debug("endElement.xmlPath: %1$s ", this.xmlPath.toString());
      logger.debug("endElement.objectPath: %1$s", this.objectPath.toString());
      logger.debug("endElement.collectionPath: %1$s", this.collectionPath.toString());
    } finally
    {
      this.clearContent();
      tracer.exit();
    }
  }
}
