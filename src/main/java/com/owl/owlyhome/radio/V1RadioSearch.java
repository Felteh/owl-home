package com.owl.owlyhome.radio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class V1RadioSearch {

    public final String query;

    @JsonCreator
    public V1RadioSearch(@JsonProperty("query") String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "V1RadioSearch{" + "query=" + query + '}';
    }

}
