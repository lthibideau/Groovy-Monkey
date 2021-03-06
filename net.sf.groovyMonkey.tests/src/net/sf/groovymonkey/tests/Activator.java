package net.sf.groovymonkey.tests;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator 
extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "net.sf.groovyMonkey.tests";
    private static Activator plugin;
    private static BundleContext context;

    public Activator()
    {
        plugin = this;
    }
    @Override
    public void start( final BundleContext context ) 
    throws Exception
    {
        super.start( context );
        Activator.context = context;
    }
    @Override
    public void stop( final BundleContext context ) 
    throws Exception
    {
        plugin = null;
        Activator.context = null;
        super.stop( context );
    }
    public static Activator getDefault()
    {
        return plugin;
    }
    public static Bundle bundle()
    {
        return getDefault().getBundle();
    }
    public static BundleContext context()
    {
        return context;
    }
}
