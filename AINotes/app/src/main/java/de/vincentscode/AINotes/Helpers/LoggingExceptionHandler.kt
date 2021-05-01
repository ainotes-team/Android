package de.vincentscode.AINotes.Helpers

class LoggingExceptionHandler : Thread.UncaughtExceptionHandler {

    private var rootHandler: Thread.UncaughtExceptionHandler? = null

    init {
        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Logger.log("Uncaught Exception", e.stackTrace.toString() + " on thread " + t.id)
    }
}