package org.conv;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.conv.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.nassimus.thread.BufferedBatchFlowControlExecutor;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcjplayer.VlcjPlayer;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Nassim MOUALEK
 * @since 23/04/2022
 */
public class XSClient {
    private String server;
    private String username;
    private String password;
    private String token;
    private String mac;
    @Setter
    @Getter
    private MediaItem selectedItem;
    @Getter
    private MediaStreamType selectedMediaType = MediaStreamType.itv;

    public String getExpirationDate() throws Exception {
        return (String) ((JSONObject) ((JSONObject) performGetAction(this.server,
                "type=account_info&action=get_main_info&mac="+this.mac )).get("js")).get("phone");
    }
    public Category[] getLiveCategories() throws Exception {
        return getCategories(MediaStreamType.itv);
    }
    public Category[] getVODCategories() throws Exception {
        return getCategories(MediaStreamType.vod);
    }
    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    private Map<String, Category> categoryMap = new HashMap<>();
    public Category[] getCategories(MediaStreamType type) throws Exception {
        String action = type.equals(MediaStreamType.itv)?"get_genres":"get_categories";
        JSONObject r = (JSONObject) performGetAction(this.server,
                "type=" + type + "&action=" + action + "&JsHttpRequest=1-xml&mac=" + this.mac);
        JSONArray val = ((JSONArray) r.get("js"));
        Category[] categories = mapper.readValue(val.toJSONString(), Category[].class);
        for (Category c: categories) {
            c.setType(type);
            categoryMap.put(c.getId(), c);
        }
        return categories;
    }
    private LazyResponse getItems(MediaStreamType type, String catId, String movieId, boolean isEpisode) throws Exception {
        AtomicInteger page = new AtomicInteger(1);
        AtomicInteger total = new AtomicInteger(0);
        LazyResponse lazyResponse = new LazyResponse(new CopyOnWriteArrayList<>());
        lazyResponse.getItems().add(new BackItem());
        lazyResponse.getItems().add(new BackItem());
        getMediaItems(lazyResponse, type, catId, movieId, isEpisode, page, total);
        return lazyResponse;
    }
    BufferedBatchFlowControlExecutor<String> processRows = null;
    private MediaItem[] getMediaItems(LazyResponse lazyResponse, MediaStreamType type, String catId, String movieId, boolean isEpisode,
                                      AtomicInteger page, AtomicInteger total) throws IOException {
        String query = "action=get_ordered_list&force_ch_link_check=&fav=0&sortby=number" +
                "&hd=0&JsHttpRequest=1-xml&from_ch_id=0&type=" + type + "&p=" + page +
                "&season_id=0&episode_id=0" ;
        Class resType = StreamItem[].class;
        if (type==MediaStreamType.series) {
            query += "&category=" + catId;
            resType = SeriesItem[].class;
        } else {
            query += "&genre=" + catId;
        }
        if (isEpisode)
            query += "&genre=*";
        if (movieId!=null)
            query += "&movie_id=" + URLEncoder.encode(movieId, "UTF8");
        JSONObject r = (JSONObject) performGetAction(this.server, query);

        JSONObject jsonObject = ((JSONObject) r.get("js"));
        JSONArray val = (JSONArray) jsonObject.get("data");
        if (total.get()==0 && val.size()!=0) {
            Long total_items = (Long) jsonObject.get("total_items");
            lazyResponse.setSize(total_items);
            total.set((int) (total_items / val.size()));
        }
        MediaItem[] mediaItems = (MediaItem[]) mapper.readValue(val.toJSONString(), resType);
        for (MediaItem c: mediaItems) {
            String catId_ = c instanceof StreamItem?((StreamItem)c).getTv_genre_id():c.getCategory_id();
            Category cat = categoryMap.get(catId_);
            if (cat==null)
                cat = categoryMap.get("*");
            c.setParent(cat);
            lazyResponse.getItems().add(lazyResponse.getItems().size()-1, c);
        }
        if (page.get()==1) {
            processRows =
                    new BufferedBatchFlowControlExecutor<>(
                            batchValues -> {
                                try {
                                    getMediaItems(lazyResponse, type, catId, movieId, isEpisode, page, total);
                                    portalPanel.rerenderItems();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }, 1, BufferedBatchFlowControlExecutor.getNbCores(), 10, "processRows") {

                        @Override
                        public void handleException(Exception e) {
                            /* The executor will throw the exception at the end if any exception */
                        }

                    };
            new Thread(
                    ()->{
                        while (page.incrementAndGet() <= total.get()) {
                            try {
                                processRows.submit("");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).run();
        }
        return mediaItems;
    }
    public LazyResponse setSelectedMediaType(MediaStreamType selectedMediaType) throws Exception {
        this.selectedMediaType = selectedMediaType;
        return open(null, null);
    }
    private Category[] loadCategories() throws Exception {
        return getCategories(selectedMediaType);
    }

    private String createLink(MediaStreamType type, String cmd, String series) throws IOException {
        String query = "action=create_link&forced_storage=0&disable_ad=0" +
                "&download=0&force_ch_link_check=0&JsHttpRequest=1-xml&cmd="+ cmd + "&type=" + type;
        if (series!=null)
            query += "&series="+series;
        JSONObject c0 = (JSONObject) performGetAction(this.server, query);
        String gcmd = (String) ((JSONObject) c0.get("js")).get("cmd");
        String cmd_ = gcmd.replace("ffmpeg ", "");
        return cmd_;
    }
    public LazyResponse open() throws Exception {
        return open(null, null);
    }
    Map<Object, LazyResponse> cache = new HashMap<>();
    public LazyResponse open(ListItem parent, String catId) throws Exception {
        Object key = parent!=null?parent:selectedMediaType;
        LazyResponse lazyResponse = cache.get(key);
        if (lazyResponse!=null)
            return lazyResponse;
        if (catId!=null)
            lazyResponse = getItems(selectedMediaType, catId, null, false);
        else
        if (parent==null) {
            Category[] categories = loadCategories();
            lazyResponse = new LazyResponse(Arrays.asList(categories));
        } else {
            MediaItem parent_ = (MediaItem) parent;
            try {
                if (parent instanceof SeriesItem) {
                    SeriesItem st = (SeriesItem) parent;
                    if (st.getSeries() != null && st.getSeries().length > 0) {
                        List<ListItem> items = new ArrayList<>();
                        items.add(new BackItem());
                        for (long l : st.getSeries())
                            items.add(new EpisodeItem(String.valueOf(l), "Episode " + l, parent_));
                        items.add(new BackItem());
                        lazyResponse = new LazyResponse(items);
                    } else {
                        lazyResponse = getItems(selectedMediaType, parent_.getCategory_id(), parent.getId(), false);
                    }
                } else if (parent instanceof EpisodeItem) {
                    SeriesItem mp = ((SeriesItem) parent_.getParent());
                    //getMediaItems(items, selectedMediaType,  parent.getId(), parent.getId(), true, new AtomicInteger(), new AtomicInteger());
                    advancedPlay(createLink(MediaStreamType.vod, mp.getCmd(), parent.getId()));
                } else
                    advancedPlay(createLink(selectedMediaType, "http://localhost/ch/43346_", null));

                selectedItem = parent_;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cache.put(key, lazyResponse);;
        return lazyResponse;
    }
    private VlcjPlayer player = new VlcjPlayer();

    private void advancedPlay(String url) throws InterruptedException {
        System.out.println(url);
        player.play(url);
    }
    PortalPanel portalPanel;
    public void showPlayer() {
        portalPanel = new PortalPanel( player, this );
        portalPanel.showPlayer();
    }

    static private void basicPlay(String url) {
        SwingUtilities.invokeLater(() -> {
                final JFrame frame;
                EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
                frame = new JFrame("vlcj quickstart");
                frame.setLocation(50, 50);
                frame.setSize(900, 600);
                frame.setContentPane(mediaPlayerComponent);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                mediaPlayerComponent.mediaPlayer().media().play(url);
        });
    }
    public UserInfo getUserInfo() {
        try {
            JSONObject a_ = (JSONObject) performGetAction(this.server.replace("portal.php", "player_api.php") ,
                    "username=" + URLEncoder.encode(this.username, "UTF-8")+"&password="+
                            URLEncoder.encode(this.password, "UTF-8"));
            JSONObject a = (JSONObject) a_.get("user_info");
            System.out.println(a.toJSONString());
            return new UserInfo((String) a.get("username"),
                    (String) a.get("password"),
                    (String) a.get("status"),
                    (String) a.get("exp_date"),
                    (String) a.get("max_connections"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public Info connect(String url, String mac) throws Exception {
            cache.clear();
            URI xuri = new URI(url);
            this.server = "http://" + xuri.getHost();
            if (xuri.getPort() > 0)
                this.server = this.server + ":" + xuri.getPort();
            this.server = server + "/portal.php";
            new URL(this.server);
            this.mac = mac;
            if (!this.getToken())
                throw new RuntimeException("ERROR: No Authorization Token\n");
            if (!this.getProfile())
                throw new RuntimeException("ERROR: No Valid Account\n");
            if (!this.getLink())
                throw new RuntimeException("ERROR: No Connection to Xtream Server\n");
            portalPanel.pushData( open() );
            System.out.println(getUserInfo());
        return new Info(url, username, password);
    }
    private Boolean getProfile() {
        try {
            JSONObject out = (JSONObject) this.performGetAction(this.server, "type=stb&action=get_profile&JsHttpRequest=1-xml");
            System.out.println(out.toJSONString());
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    private Boolean getLink() {
        try {
            JSONObject out = (JSONObject) this.performGetAction(this.server, "action=get_ordered_list&type=vod&p=1&JsHttpRequest=1-xml");
            JSONObject js = (JSONObject)out.get("js");
            JSONArray res1 = (JSONArray)js.get("data");
            JSONObject res3 = (JSONObject)res1.get(1);
            String cmd = res3.get("cmd").toString();
            if (cmd.length()<5)
                return false;
            out = (JSONObject) this.performGetAction(this.server, "action=create_link&type=vod&cmd=" + cmd + "&JsHttpRequest=1-xml");
            js = (JSONObject)out.get("js");
            String cmd_str = js.get("cmd").toString();
            String[] parts = cmd_str.split("/");
            if (parts.length > 4) {
                int idx_1 = parts.length - 2;
                int idx_2 = parts.length - 3;
                this.username = parts[idx_2];
                this.password = parts[idx_1];
                return true;
            } else {
                return false;
            }
        } catch (Exception var7) {
            return false;
        }
    }

    private Boolean getToken() {
        try {
            JSONObject out = (JSONObject) this.performGetAction(this.server, "action=handshake&type=stb&token=");
            JSONObject js = (JSONObject)out.get("js");
            this.token = js.get("token").toString();
            System.out.println(token);
            return true;
        } catch (Exception var3) {
            return false;
        }
    }
    private Object performGetAction(String url, String urlParameters) throws IOException {
        return performGetAction("GET", url, urlParameters);
    }
    private Object performGetAction(String method, String url, String urlParameters) throws IOException {
        URL obj = new URL(url + "?" + urlParameters);
        System.out.println(obj);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        String cookies = "mac=" + URLEncoder.encode(this.mac, "UTF-8") + "; stb_lang=en; timezone=Europe%2FParis; ";

        con.setRequestProperty("Cookie", cookies);

        con.setInstanceFollowRedirects(true);

        if (this.token != null)
            con.setRequestProperty("Authorization", "Bearer " + this.token);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuffer response = new StringBuffer();

        String inputLine;
        while((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        Object jout = JSONValue.parse(response.toString());
        return jout;
    }

    public VlcjPlayer getPlayer() {
        return player;
    }
}
