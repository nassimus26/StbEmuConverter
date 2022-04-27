package org.conv.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape;

/**
 * @author Nassim MOUALEK
 * @since 26/04/2022
 */
@Data
public class StreamItem extends MediaItem {

    private ListItem parent;

    private ListItem movie;
    private ListItem season;
    private String nimble_dvr;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean enable_tv_archive;
    private String use_http_tmp_link;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean allow_pvr;
    private String cmd_3;
    private String cmd_1;
    private String cmd_2;
    private String tv_genre_id;
    private String wowza_dvr;
    private String correct_time;
    private String number;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean allow_remote_pvr;
    private String service_id;
    private Object cmds; //"cmds":[{"use_http_tmp_link":"1","enable_balancer_monitoring":"0","priority":"0","url":"","nginx_secure_link":"1","wowza_tmp_link":"0","user_agent_filter":"","ch_id":"269058","enable_monitoring":"0","flussonic_tmp_link":"0","id":"269058","use_load_balancing":"0","status":"1","changed":""}]
    private String enable_monitoring;
    private String logo;
    private String modified;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean use_load_balancing;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean locked;
    private Object epg;
    private String enable_wowza_load_balancing;
    private String allow_local_timeshift;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean allow_local_pvr;
    private String monitoring_status;
    private String bonus_ch;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean archive;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean pvr;
    private int tv_archive_duration;
    private String cur_playing;
    private String mc_cmd;
    private String wowza_tmp_link;
    private String xmltv_id;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean open;
    private String base_ch;
    @JsonFormat(shape = Shape.NUMBER)
    private boolean is_series;

}
