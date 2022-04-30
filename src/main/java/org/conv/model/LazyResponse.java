package org.conv.model;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Nassim MOUALEK
 * @since 27/04/2022
 */
@Data
public class LazyResponse {

    final List<ListItem> items;
    @Setter
    long size;

    public LazyResponse(List<ListItem> items) {
        this.items = items;
        this.size= items.size();
    }
}
