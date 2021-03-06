/*
 * Created on Feb 15, 2004
 */
package groovy.swt.factory;
import java.util.Map;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class AwtSwtFactory 
extends AbstractSwtFactory 
implements SwtFactory
{
    /*
     * @see groovy.swt.impl.Factory#newInstance(java.util.Map, java.lang.Object)
     */
    @Override
    public Object newInstance( final Map< String, Object > properties, 
                               final Object parent )
    {
        if( parent instanceof Composite )
            return SWT_AWT.new_Frame( ( Composite )parent );
        return null;
    }
}
