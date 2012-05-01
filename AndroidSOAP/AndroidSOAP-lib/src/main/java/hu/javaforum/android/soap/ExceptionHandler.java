/**
 * CC-LGPL 2.1
 * http://creativecommons.org/licenses/LGPL/2.1/
 */
package hu.javaforum.android.soap;

import hu.javaforum.commons.ReflectionUtil;
import hu.javaforum.logger.Logger;
import hu.javaforum.logger.PerfLogger;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Builds the tree of the objects from the SOAP XML
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
public class ExceptionHandler extends ParserHelper
{

  /**
   * The LOGGER instance.
   */
  private static final Logger LOGGER = new Logger(ExceptionHandler.class.getSimpleName());
  /**
   * The exception root class
   */
  private final Class exceptionClass; // TODO: not thread-safe
  private Object result;
  /**
   * The exception message
   */
  private String faultString; // TODO: not thread-safe
  /**
   * The exception message
   */
//  private String faultCode; // TODO: not thread-safe
  /**
   * The path in the SOAP XML
   */
  private final List<String> xmlPath = new ArrayList<String>();
  /**
   * The objects in the XML
   */
  private final List<Object> objectPath = new ArrayList<Object>();
  /**
   * The collection objects in the XML
   */
  private final List<Object> collectionPath = new ArrayList<Object>();
  /**
   * The ArrayFieldInfo list
   */
  private final Stack<GenericHandler.ArrayFieldInfo> arrayFieldData = new Stack<GenericHandler.ArrayFieldInfo>();

  /**
   * Constructor used in development.
   *
   * @param resultClass
   * The class of the result tag
   */
  public ExceptionHandler(Class resultClass)
          throws InstantiationException, IllegalAccessException
  {
    this.exceptionClass = resultClass;
  }

  /**
   * Gets the result object
   *
   * @return The object
   */
  public Object getObject()
  {
    PerfLogger logger = new PerfLogger(LOGGER);
    // build the exception
    Constructor c;
    try
    {
      c = this.exceptionClass.getConstructor(new Class[]
              {
                String.class, result.getClass()
              });
      return c.newInstance(faultString, result);
    } catch (SecurityException e)
    {
      logger.error(e.toString(), e);
    } catch (NoSuchMethodException e)
    {
      logger.error(e.toString(), e);
    } catch (IllegalArgumentException e)
    {
      logger.error(e.toString(), e);
    } catch (InstantiationException e)
    {
      logger.error(e.toString(), e);
    } catch (IllegalAccessException e)
    {
      logger.error(e.toString(), e);
    } catch (InvocationTargetException e)
    {
      logger.error(e.toString(), e);
    }
    return null;
  }

  /**
   * Start element hook
   *
   * @param originalName
   */
  public void startElement(String originalName)
  {
    PerfLogger logger = new PerfLogger(LOGGER);

    this.clearContent();

    // need to convert the first char to lowercase for non-Java webservices
    StringBuilder sb = new StringBuilder(originalName);
    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
    String name = sb.toString();

    /**
     * Hide the SOAP envelope
     */
    if (name.indexOf("env:") == 0)
    {
      logger.debug("Skipping " + name);
      return;
    }

    if (name.equals("faultcode"))
    {
      this.objectPath.add(null);
      this.collectionPath.add(null);
    } else if (name.equals("faultstring"))
    {
      this.objectPath.add(null);
      this.collectionPath.add(null);
    } else if (name.equals("detail"))
    {
      this.objectPath.add(null);
      this.collectionPath.add(null);
    } else if (this.objectPath.size() == 1)
    {
      try
      {
        this.result = this.exceptionClass.getDeclaredField(name).getType().newInstance();
        this.objectPath.add(result);
        this.collectionPath.add(null);
      } catch (Exception except)
      {
        logger.error(except.toString(), except);
      }
    } else
    {
      /**
       * Gets the last object...
       */
      Object object = this.objectPath.get(this.objectPath.size() - 1);
      Class fieldClass = null;
      try
      {
        fieldClass = ReflectionUtil.getField(object.getClass(), name).getType();
      } catch (NoSuchFieldException ex)
      {
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
        List listObject = null;
        if (fieldClass.equals(List.class))
        {
          listObject = (List) ReflectionUtil.invokeGetter(object, name);
          this.collectionPath.add(listObject);
        } else
        {
          // this.xmlPath.get(xmlPath.size()-1).equals(anObject) =
          // name;
          GenericHandler.ArrayFieldInfo afi = null;
          if (this.arrayFieldData.size() > 0)
          {
            afi = this.arrayFieldData.peek();
          }
          if (afi == null)
          {
            afi = new GenericHandler.ArrayFieldInfo(name, this.xmlPath.size());
            this.arrayFieldData.push(afi);
          }
          listObject = afi.getArrayData();
          this.collectionPath.add(null);
        }

        /**
         * Creates a new instance, and put it into the objects
         * collection
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
         * Creates a new simple instance, and put it into the objects
         * collection
         */
        try
        {
          if (fieldClass.isPrimitive() || fieldClass.isArray())
          {
            this.objectPath.add(fieldClass);
            this.collectionPath.add(null);
          } else if (fieldClass.getName().equals("java.math.BigDecimal"))
          {
            // special handling for BigDecimal
            Object fieldObject = fieldClass.getDeclaredConstructor(int.class).newInstance(0);
            this.objectPath.add(fieldObject);
            this.collectionPath.add(null);
            Object parentObject = this.objectPath.get(this.objectPath.size() - 2);
            ReflectionUtil.invokeSetter(parentObject, name, fieldObject);
          } else if (fieldClass.isEnum())
          {
            // small hack to handle enums
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
        } catch (NoSuchMethodException except)
        {
          logger.error(except.toString(), except);
        } catch (SecurityException except)
        {
          logger.error(except.toString(), except);
        } catch (InstantiationException except)
        {
          logger.error(except.toString(), except);
        } catch (IllegalAccessException except)
        {
          logger.error(except.toString(), except);
        } catch (IllegalArgumentException except)
        {
          logger.error(except.toString(), except);
        } catch (InvocationTargetException except)
        {
          logger.error(except.toString(), except);
        }
      }
    }

    this.xmlPath.add(name);

    logger.debug("startElement.xmlPath: " + this.xmlPath.toString());
    logger.debug("startElement.objectPath: " + this.objectPath.toString());
    logger.debug("startElement.collectionPath: " + this.collectionPath.toString());
  }

  /**
   * End element hook
   *
   * @param originalName The name of the tag
   */
  public void endElement(String originalName)
  {
    PerfLogger logger = new PerfLogger(LOGGER);

    if (originalName.indexOf("env:") == 0)
    {
      // not processing Envelope elements
      return;
    }

    // need to convert the first char to lowercase for non-Java webservices
    StringBuilder sb = new StringBuilder(originalName);
    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
    String name = sb.toString();

    this.xmlPath.remove(this.xmlPath.size() - 1);
    /**
     * if the parse depth drops back to where we first encountered array
     * elements, gather the assembled array elements and call the parent
     * object's array setter TODO: now handling array fields CJW
     */
    if (this.arrayFieldData != null && this.arrayFieldData.size() > 0)
    {
      ArrayFieldInfo afi = this.arrayFieldData.peek();
      if (this.xmlPath.size() < afi.getArrayFieldDepth())
      {
        this.arrayFieldData.pop();
        Object object = this.objectPath.get(this.objectPath.size() - 1);
//        logger.debug("invokeSetter(" + object + ", " + afi.getFieldName() + ", " + afi.getArrayData() + ")");
        ReflectionUtil.invokeSetter(object, afi.getFieldName(), afi.getArrayData());
      }
    }

    if (originalName.equals("faultcode"))
    {
      this.objectPath.remove(null);
      this.collectionPath.remove(this.collectionPath.size() - 1);
//      this.faultCode = getContent();
    } else if (originalName.equals("faultstring"))
    {
      this.objectPath.remove(null);
      this.collectionPath.remove(this.collectionPath.size() - 1);
      this.faultString = getContent();
    } else if (originalName.equals("detail"))
    {
      this.objectPath.remove(null);
      this.collectionPath.remove(this.collectionPath.size() - 1);
    } else if (this.objectPath.size() == 2)
    {
      this.objectPath.remove(result);
      this.collectionPath.remove(this.collectionPath.size() - 1);
    } else
    {
      Object object = this.objectPath.get(this.objectPath.size() - 2);
      Class fieldClass = null;
      try
      {
        fieldClass = ReflectionUtil.getField(object.getClass(), name).getType();
      } catch (NoSuchFieldException ex)
      {
      }

      /**
       * Save the content, if the field is String - data conversions occur
       * in invokeSetter
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

    logger.debug("endElement.xmlPath: " + this.xmlPath.toString());
    logger.debug("endElement.objectPath: " + this.objectPath.toString());
    logger.debug("endElement.collectionPath: " + this.collectionPath.toString());

    this.clearContent();
  }
}
