package org.conv;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.conv.model.*;
import uk.co.caprica.vlcjplayer.VlcjPlayer;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;


/**
 * @author Nassim MOUALEK
 * @since 26/04/2022
 */
@RequiredArgsConstructor
public class PortalPanel {
    @Getter
    private final Map<MediaStreamType, JList<ListItem>> jListByType = new HashMap<>();
    private final JTextField filter = new JTextField();
    private final JTextField url = new JTextField();
    private final JTextField mac = new JTextField();
    private final JButton connect = new JButton();
    private final VlcjPlayer player;
    private final XSClient xStreamClient;

    public void showPlayer() {
        JPanel leftPanel = player.getLeftPanel();
        JPanel urlPanel = new JPanel(new GridBagLayout());
        JPanel macPanel = new JPanel(new GridBagLayout());
        JPanel gridPanel = new JPanel();
        JLabel urlLabel = new JLabel("URL ");
        //urlLabel.setLabelFor(url);
        urlPanel.add(urlLabel);
        urlPanel.add(url);
        urlPanel.setPreferredSize(new Dimension(240, 20));
        macPanel.setPreferredSize(new Dimension(240, 20));
        JLabel macLabel = new JLabel("MAC ");
        //macLabel.setLabelFor(mac);
        macPanel.add(macLabel);
        mac.setMinimumSize(new Dimension( 136, 16 ));
        url.setPreferredSize(new Dimension( 200, 16 ));

        macPanel.add(mac);
        connect.setText("Connect");
        macPanel.add(connect);
        connect.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    xStreamClient.connect(url.getText(), mac.getText());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        leftPanel.add(gridPanel);
        gridPanel.add(urlPanel);
        gridPanel.add(macPanel);

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
                        LazyResponse items = null;
                        if (list.getSelectedValue() instanceof BackItem) {
                            back();
                        } else {
                            if (list.getSelectedValue() instanceof Category) {
                                Category category = (Category) list.getSelectedValue();
                                try {
                                    items = xStreamClient.open(category, category.getId());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                try {
                                    items = xStreamClient.open((MediaItem) list.getSelectedValue(), null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (items!=null)
                                pushData(items);
                        }
                        rerender();
                    }
                }
            });
        }
        tabs.addChangeListener(e -> {
            try {
                LazyResponse lazyResponse = xStreamClient.setSelectedMediaType(MediaStreamType.values()[tabs.getSelectedIndex()]);
                pushData(lazyResponse);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        tabs.setEnabledAt(0, true);
        JPanel filterPanel = new JPanel();

        filter.setPreferredSize(new Dimension(185, 20));
        filter.setMinimumSize(new Dimension(185, 20));

        JLabel filterLabel = new JLabel("Filter :");
        filterLabel.setLabelFor(filter);
        filterPanel.add(filterLabel);
        filterPanel.add(filter);
        filterPanel.setPreferredSize(new Dimension(240, 30));
        gridPanel.add(filterPanel);
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.setPreferredSize(new Dimension(240, 450));
        gridPanel.add(tabs);
        gridPanel.setBackground(Color.gray);
        gridPanel.setPreferredSize(new Dimension(240, 250));
        gridPanel.setSize(240, 250);

        leftPanel.setMinimumSize(new Dimension(240, 250));

        filter.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                MediaStreamType type = xStreamClient.getSelectedMediaType();
                LazyResponse data = stack.get(type).peek();
                if (!filter.getText().isEmpty())
                    data = new LazyResponse(data.getItems().stream().filter(a -> a.getTitle().toLowerCase().contains(filter.getText().toLowerCase())).toList());
                updateModel(data);
                rerender();
            }
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        rerender();
    }
    private Map<MediaStreamType, Stack<LazyResponse>> stack = new HashMap<>();
    public void pushData(LazyResponse data) {
        MediaStreamType type = xStreamClient.getSelectedMediaType();
        stack.putIfAbsent(type, new Stack<>());
        //jListByType.get(xStreamClient.getSelectedMediaType()).setListData(data);
        updateModel(data);
        stack.get(type).push(data);
        rerender();
    }

    private void updateModel(LazyResponse data) {
        jListByType.get(xStreamClient.getSelectedMediaType()).setModel(new ListModel<>() {
            LazyResponse data_ = data;
            @Override
            public int getSize() {
                return data_.getItems().size();
            }

            @Override
            public ListItem getElementAt(int index) {
                return data_.getItems().get(index);
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        });
    }

    public void back() {
        try {
            MediaStreamType type = xStreamClient.getSelectedMediaType();
            stack.get(type).pop();
            updateModel(stack.get(type).peek());
        } catch (Exception e) {
            e.printStackTrace();
        }
        rerender();
    }
    public void rerenderItems() {
        MediaStreamType type = xStreamClient.getSelectedMediaType();
        updateModel(stack.get(type).peek());
        rerender();
    }
    public void rerender() {
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
