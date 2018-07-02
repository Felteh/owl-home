import React from 'react';
import List from "@material-ui/core/es/List/List";
import ListSubheader from "@material-ui/core/es/ListSubheader/ListSubheader";
import Snackbar from "@material-ui/core/es/Snackbar/Snackbar";

export class VideoListingPage extends React.Component {
    render() {
        return (
            <div>
                <VideoToolbar/>
                <VideoListing/>
            </div>
        );
    }
}

class VideoToolbar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {errorOpen: false, errorMsg: "No issue"};
    }

    resume() {
        let this_ = this;
        console.log("Requesting from /videos/resume");
        return fetch('/videos/resume').then(function (response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
            this_.setState({errorOpen: true, errorMsg: error.message});
        });
    }

    pause() {
        let this_ = this;
        console.log("Requesting from /videos/pause");
        return fetch('/videos/pause').then(function (response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
            this_.setState({errorOpen: true, errorMsg: error.message});
        });
    }

    stop() {
        let this_ = this;
        console.log("Requesting from /videos/stop");
        return fetch('/videos/stop').then(function (response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
            this_.setState({errorOpen: true, errorMsg: error.message});
        });
    }

    render() {
        return (
            <div>
                <Paper zDepth={2}>
                    <RaisedButton label="Resume" fullWidth={true} secondary={true} onTouchTap={() => this.resume()}/>
                    <RaisedButton label="Pause" fullWidth={true} secondary={true} onTouchTap={() => this.pause()}/>
                    <RaisedButton label="Stop" fullWidth={true} secondary={true} onTouchTap={() => this.stop()}/>
                </Paper>
                <Snackbar
                    open={this.state.errorOpen}
                    message={this.state.errorMsg}
                    autoHideDuration={2000}
                />
            </div>
        );
    }
}

class VideoListing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            videos: [],
            errorOpen: false,
            errorMsg: "No issue"
        };
    }

    componentDidMount() {
        let this_ = this;
        console.log("Requesting from /videos");
        fetch('/videos')
            .then(function (response) {
                if (!response.ok) {
                    throw Error(response.statusText);
                }
                return response.json();
            })
            .then(function (response) {
                this_.setState({videos: response, errorOpen: this_.state.errorOpen, errorMsg: this_.state.errorMsg});
            })
            .catch(function (error) {
                console.log("EPIC FAIL ON QUERY");
                console.error(error);
                this_.setState({videos: [], errorOpen: true, errorMsg: error.message});
            });
    }

    render() {
        return (
            <div>
                <List>
                    <ListSubheader>Videos</ListSubheader>
                    {
                        this.state.videos.map(
                            function (v) {
                                return (
                                    <VideoListItem
                                        key={v.path}
                                        video={v}
                                    />
                                );
                            }
                        )
                    }
                </List>
                <Snackbar
                    open={this.state.errorOpen}
                    message={this.state.errorMsg}
                    autoHideDuration={2000}
                />
            </div>
        );
    }
}


class VideoListItem extends React.Component {
    play(video) {
        console.log("Requesting from /videos/play");
        return fetch('/videos/play', {
            method: "POST",
            headers: new Headers({"Content-Type": "application/json"}),
            body: JSON.stringify({filename: video.path, audio: 'both'})
        }).then(function (response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
        });
    }

    render() {
        return (
            <ListItem
                onTouchTap={() => this.play(this.props.video)}
                primaryText={this.props.video.name + " " + this.props.video.length + "mb"}
                secondaryText={this.props.video.path}
            />
        );
    }
}