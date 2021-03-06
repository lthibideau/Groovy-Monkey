/*******************************************************************************
 * Copyright (c) 2005, 2006 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/
package net.sf.groovyMonkey;
import static java.util.Collections.synchronizedMap;
import static net.sf.groovyMonkey.UpdateMonkeyActionsResourceChangeListener.createTheMonkeyMenu;
import static net.sf.groovyMonkey.preferences.PreferenceInitializer.MONKEY_MENU_NAME;
import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.sf.groovyMonkey.util.SetUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class GroovyMonkeyPlugin 
extends AbstractUIPlugin 
implements IStartup
{
    public static final String PLUGIN_ID = "net.sf.groovyMonkey";
    public static final String PUBLISH_BEFORE_MARKER = "--- Came wiffling through the eclipsey wood ---";
    public static final String PUBLISH_AFTER_MARKER = "--- And burbled as it ran! ---";
    public static final String SCRIPT_SUFFIX = "gm";
    public static final String FILE_EXTENSION = "." + SCRIPT_SUFFIX;
    public static final String FILE_EXTENSION_WILDCARD = "*" + FILE_EXTENSION;
    public static final String MONKEY_DIR = "monkey";
    public static final String SCRIPTS_PROJECT = "GroovyMonkeyScripts";
    public static final String MENU_PATH = "groovyMonkeyMenu";
    public static final String MENU_EDIT_PATH = "groovyMonkeyEditMenu";
    public static final String ICON_PATH = "icons/";
    private static GroovyMonkeyPlugin plugin;
    private static BundleContext context;
    private static final Map< String, ScriptMetadata > scriptStore = synchronizedMap( new HashMap< String, ScriptMetadata >() );
    private ServiceTracker tracker = null;
    private boolean started = false;
    
    public static Map< String, ScriptMetadata > getScriptStore()
    {
        return scriptStore;
    }
    public static Map< String, ScriptMetadata > scriptStore()
    {
        return getScriptStore();
    }
    public GroovyMonkeyPlugin()
    {
        plugin = this;
    }
    @Override
    public void start( final BundleContext context ) 
    throws Exception
    {
        super.start( context );
        earlyStartup();
        GroovyMonkeyPlugin.context = context;
        tracker = new ServiceTracker( context, PlatformAdmin.class.getName(), null );
        tracker.open();
    }
    @Override
    public void stop( final BundleContext context )
    throws Exception
    {
        super.stop( context );
        plugin = null;
        if( tracker != null )
            tracker.close();
    }
    public static GroovyMonkeyPlugin getDefault()
    {
        return plugin;
    }
    public static ImageDescriptor getImageDescriptor( final String path )
    {
        return imageDescriptorFromPlugin( PLUGIN_ID, path );
    }
    public void earlyStartup()
    {
    	if( started )
    		return;
    	started = true;
        final UpdateMonkeyActionsResourceChangeListener listener = new UpdateMonkeyActionsResourceChangeListener();
        getWorkspace().addResourceChangeListener( listener, POST_CHANGE );
        listener.rescanAllFiles();
        final IPropertyChangeListener propertyListener = new IPropertyChangeListener()
        {
            public void propertyChange( final PropertyChangeEvent event )
            {
                if( event == null )
                    return;
                if( !MONKEY_MENU_NAME.equals( event.getProperty() ) )
                    return;
                createTheMonkeyMenu();
            }            
        };
        getPreferenceStore().addPropertyChangeListener( propertyListener );
        createTheMonkeyMenu();
    }
    public static void addScript( final String name, 
                                  final ScriptMetadata script )
    {
        final Map< String, ScriptMetadata > store = scriptStore();
        final ScriptMetadata oldScript = store.get( name );
        if( oldScript != null )
            oldScript.unsubscribe();
        store.put( name, script );
        script.subscribe();
    }
    public static void removeScript( final String name )
    {
        final Map< String, ScriptMetadata > store = scriptStore();
        final ScriptMetadata oldScript = store.remove( name );
        if( oldScript == null )
            return;
        oldScript.unsubscribe();
    }
    public static void clearScripts()
    {
        final Set< String > keys = new LinkedHashSet< String >( scriptStore().keySet() );
        for( final String key : keys )
            removeScript( key );
        scriptStore().clear();
    }
    public static ImageRegistry registry()
    {
        return getDefault().getImageRegistry();
    }
    public static Image icon( final String name )
    {
        final Image image = registry().get( name );
        if( image != null )
            return image;
        registry().put( name, getImageDescriptor( ICON_PATH + name ) );
        return registry().get( name );
    }
    public static BundleContext context()
    {
        return context;
    }
    public static Bundle bundle()
    {
        return getDefault().getBundle();
    }
    public static PlatformAdmin getPlatformAdmin()
    {
        return ( PlatformAdmin )getDefault().tracker.getService();
    }
    public static PlatformAdmin platformAdmin()
    {
        return getPlatformAdmin();
    }
    public static BundleDescription getBundleDescription( final long id )
    {
        return platformAdmin().getState().getBundle( id );
    }
    public static BundleDescription bundleDescription( final long id )
    {
        return getBundleDescription( id );
    }
    public static BundleDescription getBundleDescription( final String name )
    {
        return platformAdmin().getState().getBundle( name, null );
    }
    public static BundleDescription bundleDescription( final String name )
    {
        return getBundleDescription( name );
    }
    public static BundleDescription getBundleDescription()
    {
        return getBundleDescription( bundle().getBundleId() );
    }
    public static BundleDescription bundleDescription()
    {
        return getBundleDescription();
    }
    public static Set< String > getAllRequiredBundles( final long id )
    {
        final Set< String > set = new LinkedHashSet< String >();
        final BundleDescription description = bundleDescription( id );
        addRequiredBundles( set, description );
        for( final BundleDescription bundleDescription : description.getFragments() )
            addRequiredBundles( set, bundleDescription );
        return set;
    }
    public static Set< String > getAllReexportedBundles( final long id )
    {
        final Set< String > set = new LinkedHashSet< String >();
        final BundleDescription description = bundleDescription( id );
        addReexportedBundles( set, description );
        for( final BundleDescription bundleDescription : description.getFragments() )
            addReexportedBundles( set, bundleDescription );
        return set;
    }
    public static Set< String > getAllRequiredBundles()
    {
        return getAllRequiredBundles( getDefault().getBundle().getBundleId() );
    }
    public static Set< String > getAllRequiredBundles( final String name )
    {
        return getAllRequiredBundles( bundleDescription( name ).getBundleId() );
    }
    public static Set< String > getAllReexportedBundles( final String name )
    {
        if( StringUtils.isBlank( name ) )
            return SetUtil.set();
        if( bundleDescription( name ) == null )
            return SetUtil.set();
        return getAllReexportedBundles( bundleDescription( name ).getBundleId() );
    }
    private static void addRequiredBundles( final Set< String > set, 
                                            final BundleDescription description )
    {
        for( final BundleSpecification required : description.getRequiredBundles() )
            set.add( required.getName() );
    }
    private static void addReexportedBundles( final Set< String > set, 
                                              final BundleDescription description )
    {
        for( final BundleSpecification required : description.getRequiredBundles() )
            if( required.isExported() )
                set.add( required.getName() );
    }
    public static void logException( final String message, final Throwable throwable )
    {
        log( IStatus.ERROR, message, throwable );
    }
    public static void logExceptionWarning( final Throwable throwable )
    {
        logExceptionWarning( throwable.getMessage(), throwable );
    }
    public static void logExceptionWarning( final String message, final Throwable throwable )
    {
        log( IStatus.WARNING, message, throwable );
    }
    public static void logExceptionInfo( final Throwable throwable )
    {
        logExceptionInfo( throwable.getMessage(), throwable );
    }
    public static void logExceptionInfo( final String message, final Throwable throwable )
    {
        log( IStatus.INFO, message, throwable );
    }
    public static void logError( final String message )
    {
        log( IStatus.ERROR, message, null );
    }
    public static void logError( final String message, final Throwable throwable )
    {
        log( IStatus.ERROR, message, throwable );
    }
    public static void logWarning( final String message )
    {
        log( IStatus.WARNING, message, null );
    }
    public static void logWarning( final String message, final Throwable throwable )
    {
        log( IStatus.WARNING, message, throwable );
    }
    public static void logInfo( final String message )
    {
        log( IStatus.INFO, message, null );
    }
    public static void logInfo( final String message, final Throwable throwable )
    {
        log( IStatus.INFO, message, throwable );
    }
    public static void log( final int severity, final String message, final Throwable throwable )
    {
        if( getDefault() == null )
            return;
        getDefault().getLog().log( new Status( severity, PLUGIN_ID, 0, StringUtils.defaultString( message ), throwable ) );
    }
}
