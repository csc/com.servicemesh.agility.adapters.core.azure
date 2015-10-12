/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */
package com.servicemesh.agility.adapters.core.azure;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class TestHelpers
{
    public static void initLogger(Level level)
    {
        ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout("%d [%p|%c|%C{1}] %m%n"));
        console.setThreshold(level);
        console.activateOptions();

        Logger logger = Logger.getLogger(TestHelpers.class.getPackage().getName());
        logger.removeAllAppenders();
        logger.setLevel(level);
        logger.addAppender(console);
    }

    public static Logger setLogLevel(String loggerName, Level level)
    {
        Logger logger = Logger.getLogger(loggerName);
        logger.setLevel(level);
        return logger;
    }
}
