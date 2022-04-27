package org.conv.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


/**
 * @author Nassim MOUALEK
 * @since 26/04/2022
 */
@Data
public class MediaItem  implements ListItem {
    private ListItem parent;
    private String category_id;
    boolean lock;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean fav;
    String id;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    long count;
    String name;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    long volume_correction;
    String cmd;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean status;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean censored;
    long cost;
    String genres_str;
    @Override
    public String getTitle() {
        return name;
    }

}
