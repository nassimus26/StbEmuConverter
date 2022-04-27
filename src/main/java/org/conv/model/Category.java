package org.conv.model;

import lombok.Data;
import org.conv.MediaStreamType;

/**
 * @author Nassim MOUALEK
 * @since 26/04/2022
 */
@Data
public class Category implements ListItem {
    private long number;
    private String alias;
    private String id;
    private String title;
    private String active_sub;
    private String censored;
    private String modified;
    private MediaStreamType type;
}
