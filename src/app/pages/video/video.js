import React from 'react';

export class VideoListingContainer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {videos: this.fetchVideos()};
    }

    fetchVideos() {
        console.log("Requesting from /videos");
        return fetch('/videos').then(function (response) {
            return response.json();
        }).catch(function (error) {
            console.log("EPIC FAIL ON QUERY");
            return [{'file': 'blah'}]
        });
    }

    render() {
        return (
            <VideoListing videos={this.state.videos}></VideoListing>
        );
    }
}

class VideoListing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {videos: []};
        var self = this;
        props.videos.then(function (response) {
            self.setState({videos: response});
        });
    }

    render() {
        return (
            <div>{
                this.state.videos.map(function (o) {
                    return (<h1 key={o.file}>{o.file}</h1>)
                })
            }</div>
        );
    }
}