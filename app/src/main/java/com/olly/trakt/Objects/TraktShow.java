package com.olly.trakt.Objects;

public class TraktShow {
        public String title;
        public int year;
        public TraktID ids;


        public TraktShow(String title, int year, TraktID ids){
                this.title = title;
                this.year = year;
                this.ids = ids;
        }
}
