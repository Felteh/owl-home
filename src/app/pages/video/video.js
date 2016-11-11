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

export class VideoListingPage extends React.Component {
    render() {
        return (
                <div>
                    <VideoToolbar/>
                    <VideoListingContainer/>
                </div>
                );
    }
}

class VideoToolbar extends React.Component {
    resume() {
        console.log("Requesting from /resume");
        return fetch('/resume').then(function (response) {
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
        });
    }

    pause() {
        console.log("Requesting from /pause");
        return fetch('/pause').then(function (response) {
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
        });
    }

    stop() {
        console.log("Requesting from /stop");
        return fetch('/stop').then(function (response) {
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
        });
    }

    render() {
        return (
                <Toolbar>
                    <ToolbarGroup firstChild={true}>
                        <RaisedButton label="Resume" secondary={true} onTouchTap={() => this.resume()} />
                        <RaisedButton label="Pause" secondary={true} onTouchTap={() => this.pause()} />
                        <RaisedButton label="Stop" secondary={true} onTouchTap={() => this.stop()} />
                    </ToolbarGroup>
                </Toolbar>
                );
    }
}

class VideoListingContainer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {vidRequest: this.fetchVideos()};
    }

    fetchVideos() {
        console.log("Requesting from /videos");
        return fetch('/videos').then(function (response) {
            return response.json();
        }).catch(function (error) {
            console.log("EPIC FAIL ON QUERY");
            return [{'file': 'blah'}];
        });
    }

    render() {
        return (
                <VideoListing vidRequest={this.state.vidRequest}></VideoListing>
                );
    }
}

class VideoListing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {videos: []};
        var self = this;
        props.vidRequest.then(function (response) {
            self.setState({videos: response});
        });
    }
    render() {
        return (
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
                })
                }
                </List>
                            );
            }
}


class VideoListItem extends React.Component {
    play(video) {
        console.log("Requesting from /stop");
        return fetch('/play', {
            method: "POST",
            headers:new Headers({"Content-Type": "application/json"}),
            body: JSON.stringify({filename: video.path, audio: 'both'})
        }).then(function (response) {
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
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