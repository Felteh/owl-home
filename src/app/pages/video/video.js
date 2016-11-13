import React from 'react';
import {List, ListItem} from 'material-ui/List';
import Subheader from 'material-ui/Subheader';
import Divider from 'material-ui/Divider';
import FontIcon from 'material-ui/FontIcon';
import IconButton from 'material-ui/IconButton';
import {Toolbar, ToolbarGroup, ToolbarSeparator, ToolbarTitle} from 'material-ui/Toolbar';
import DropDownMenu from 'material-ui/DropDownMenu';
import MenuItem from 'material-ui/MenuItem';
import RaisedButton from 'material-ui/RaisedButton';
import Snackbar from 'material-ui/Snackbar';

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
        var this_ = this;
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
        var this_ = this;
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
        var this_ = this;
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
                    <Toolbar>
                        <ToolbarGroup firstChild={true}>
                            <RaisedButton label="Resume" secondary={true} onTouchTap={() => this.resume()} />
                            <RaisedButton label="Pause" secondary={true} onTouchTap={() => this.pause()} />
                            <RaisedButton label="Stop" secondary={true} onTouchTap={() => this.stop()} />
                        </ToolbarGroup>
                    </Toolbar>
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
        var this_ = this;
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
                    <Subheader>Videos</Subheader>
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