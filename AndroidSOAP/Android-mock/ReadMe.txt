$Id$

This project creates a library which is a small subset of the the 
Android runtime, to allow running/debugging Android apps without
deploying to the emulator or device. Obviously, this only works
for non-UI code.  In fact, the only piece of the Android system
libaries provided are android.util.Xml and android.util.Log

This alternative library is implemented by replacing native methods 
with dummy calls, or a 100% java alternative.
