package org.conv.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nassim MOUALEK
 * @since 26/04/2022
 */
@Data
@NoArgsConstructor
public class EpisodeItem extends MediaItem {
    public EpisodeItem(String id, String name, MediaItem parent) {
        this.id = id;
        this.name = name;
        this.setParent(parent);
    }
}
