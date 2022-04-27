package org.conv;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.conv.model.BackItem;
import org.conv.model.Category;
import org.conv.model.ListItem;
import org.conv.model.MediaItem;
import uk.co.caprica.vlcjplayer.VlcjPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author Nassim MOUALEK
 * @since 26/04/2022
 */
@RequiredArgsConstructor
public class PortalPanel {
    @Getter
    private final Map<MediaStreamType, JList<ListItem>> jListByType = new HashMap<>();
    private final JTextField filter = new JTextField();
    private final VlcjPlayer player;
    private final XStreamClient xStreamClient;
    private final Map<MediaStreamType, List<Category>> categoriesMap;
    private final Map<MediaStreamType, List<MediaItem>> mediaItemsMap;

    public void showPlayer() {
        JPanel leftPanel = player.getLeftPanel();
        JPanel gridPanel = new JPanel();
        leftPanel.add(gridPanel);
        leftPanel.setLayout(new GridLayout(0, 1, 0, 0));
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize( new Dimension( 260, 0 ) );
        tabs.setTabPlacement( JTabbedPane.BOTTOM );
        for (MediaStreamType type : MediaStreamType.values()) {
            JList jList = new JList();
            jListByType.put(type, jList);
            jList.setCellRenderer(new ListCellRenderer());
            jList.setMinimumSize(new Dimension(238, 250));
            JScrollPane scrollpane = new JScrollPane(jListByType.get(type));
            scrollpane.setSize(238, 250);
            scrollpane.setBackground(Color.gray);
            scrollpane.setPreferredSize(new Dimension(240, 250));
            tabs.addTab( type.name(), scrollpane );

            jList.setCellRenderer(new ListCellRenderer());
            jList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList list = (JList)evt.getSource();
                    if (evt.getClickCount() == 2) {
                        if (list.getSelectedValue()==null)
                            return;
                        int index = list.locationToIndex(evt.getPoint());
                        list.ensureIndexIsVisible(index);
                        ((DefaultListSelectionModel) list.getSelectionModel()).moveLeadSelectionIndex(index);
                        list.getComponentAt(evt.getPoint()).setFocusable(true);
                        list.getComponentAt(evt.getPoint()).transferFocus();
                        List<MediaItem> items = null;
                        if (list.getSelectedValue() instanceof BackItem) {
                            back();
                        } else {
                            if (list.getSelectedValue() instanceof Category) {
                                Category category = (Category) list.getSelectedValue();
                                try {
                                    items = xStreamClient.getItems(category.getType(), category.getId(), null, false);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                try {
                                    xStreamClient.open((MediaItem) list.getSelectedValue());
                                    items = mediaItemsMap.get(xStreamClient.getSelectedMediaType());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            pushData(items.toArray(new MediaItem[0]));
                        }
                        rerender();
                    }
                }
            });
        }
        tabs.addChangeListener(e -> {
            try {
                xStreamClient.setSelectedMediaType(MediaStreamType.values()[tabs.getSelectedIndex()]);
                pushData(categoriesMap.get(xStreamClient.getSelectedMediaType()).toArray(new Category[0]));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        tabs.setEnabledAt(0, true);
        JPanel filterPanel = new JPanel();
        filter.setSize(185, 20);
        filter.setPreferredSize(new Dimension(185, 20));
        filter.setMinimumSize(new Dimension(185, 20));

        JLabel filterLabel = new JLabel("Filter :");
        filterLabel.setLabelFor(filter);
        filterPanel.add(filterLabel);
        filterPanel.add(filter);
        gridPanel.add(filterPanel);
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.setPreferredSize(new Dimension(240, 250));
        gridPanel.add(tabs);
        gridPanel.setBackground(Color.gray);
        gridPanel.setPreferredSize(new Dimension(240, 250));
        gridPanel.setSize(240, 250);

        leftPanel.setMinimumSize(new Dimension(240, 250));

        filter.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                MediaStreamType type = xStreamClient.getSelectedMediaType();
                ListItem[] data = stack.get(type).peek();
                if (!filter.getText().isEmpty())
                    data = Arrays.stream(data).filter(a -> a.getTitle().toLowerCase().contains(filter.getText().toLowerCase())).toList().toArray(new Category[0]);
                pushData(data);
                rerender();
            }
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        rerender();
    }
    private Map<MediaStreamType,Stack<ListItem[]>> stack = new HashMap<>();
    public void pushData(ListItem[] data) {
        MediaStreamType type = xStreamClient.getSelectedMediaType();
        stack.putIfAbsent(type, new Stack<>());
        jListByType.get(xStreamClient.getSelectedMediaType()).setListData(data);
        stack.get(type).push(data);
        rerender();
    }
    public void back() {
        try {
            MediaStreamType type = xStreamClient.getSelectedMediaType();
            stack.get(type).pop();
            jListByType.get(xStreamClient.getSelectedMediaType()).setListData(stack.get(type).peek());
            //jListByType.get(xStreamClient.getSelectedMediaType()).setListData(categoriesMap.get(xStreamClient.getSelectedMediaType()).toArray(new Category[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        rerender();
    }

    private void rerender() {
        player.getMainFrame().revalidate();
    }

    class ListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, ((ListItem)value).getTitle(), index, isSelected, cellHasFocus);
            label.setOpaque(isSelected);
            return label;
        }
    }
}
