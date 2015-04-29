package net.javapla.jawn.core.util;

import net.javapla.jawn.core.ResponseRunner;

/**
 * Empty container for rendering purposes.
 * 
 * <p>
 * It causes the {@link ResponseRunner} to render no body, just the header. Useful
 * when issuing a redirect and no corresponding content should be shown.
 * 
 * @author MTD
 */
public class NoHttpBody {
    // intentionally left empty. Just a marker class.
}
