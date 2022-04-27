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
public class SeriesItem extends MediaItem {
    String o_name;
    String year;
    String added;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean is_series;
    String week_and_more;
    String pic;
    String accessed;
    String rating_count_imdb;
    String path;
    long count_second_0_5;
    String rating_last_update;
    String protocol;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean low_quality;
    String fname;
    String rating_imdb;
    String director;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    long count_first_0_5;
    String autocomplete_provider;
    String actors;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    long cat_genre_id_2;
    String cat_genre_id_1;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    long cat_genre_id_4;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    long cat_genre_id_3;
    String screenshot_uri;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean vote_sound_bad;
    long is_movie;
    String description;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean genre_id;
    String screenshots;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean genre_id_1;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean high_quality;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean vote_video_bad;
    String last_rate_update;
    boolean genre_id_3;
    String file;
    long genre_id_2;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean vote_sound_good;
    long genre_id_4;
    String rate;
    String last_played;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean vote_video_good;
    String rating_kinopoisk;
    String owner;
    String rtsp_url;
    String comments;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean has_files;
    long year_end;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean for_rent;
    String old_name;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean for_sd_stb;
    String time;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean hd;
    String age;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    boolean disable_for_hd_devices;
    long[] series;
    String cmd;
    public SeriesItem(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
