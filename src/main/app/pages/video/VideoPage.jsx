import React from 'react';
import {GetVideos, PauseVideo, PlayVideo, ResumeVideo, StopVideo} from "./VideoApi";
import Snackbar from "@material-ui/core/es/Snackbar/Snackbar";
import ListSubheader from "@material-ui/core/es/ListSubheader/ListSubheader";
import List from "@material-ui/core/es/List/List";
import LinearProgress from "@material-ui/core/es/LinearProgress/LinearProgress";
import ListItem from "@material-ui/core/es/ListItem/ListItem";
import ListItemText from "@material-ui/core/es/ListItemText/ListItemText";
import ListItemSecondaryAction from "@material-ui/core/es/ListItemSecondaryAction/ListItemSecondaryAction";
import Paper from "@material-ui/core/es/Paper/Paper";
import Button from "@material-ui/core/es/Button/Button";

class VideoPage extends React.Component {
    state = {
        videos: [],
        loading: false,
        errorOpen: false,
        errorMsg: "No issue"
    };

    componentDidMount = () => {
        this.loadVideos();
    };

    loadVideos = () => {
        this.setState({loading: true});

        GetVideos()
            .then(response => {
                this.setState({videos: response.data, loading: false});
            })
            .catch(error => {
                this.setState({loading: false, errorOpen: true, errorMsg: error.message});
                console.log("ERROR");
                console.log(error);
            });
    };

    render() {
        const {loading, videos, errorOpen, errorMsg} = this.state;
        return (
            <div>
                <VideoToolbar/>
                <List>
                    <ListSubheader>Videos</ListSubheader>
                    {loading && <LinearProgress/>}
                    {videos &&
                    videos.map(
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
                    open={errorOpen}
                    message={errorMsg}
                    autoHideDuration={2000}
                />
            </div>
        );
    }
}

class VideoToolbar extends React.Component {
    state = {
        errorOpen: false,
        errorMsg: "No issue"
    };

    handleErrors = (promise) => {
        promise
            .then(response => {
                this.setState({errorOpen: false});
                console.log("Playing video");
                console.log(this.props.video);
            })
            .catch(error => {
                this.setState({errorOpen: true, errorMsg: error.message});
                console.log("ERROR");
                console.log(error);
            });
    };

    resume = () => {
        this.handleErrors(ResumeVideo());
    };
    pause = () => {
        this.handleErrors(PauseVideo());
    };
    stop = () => {
        this.handleErrors(StopVideo());
    };

    render() {
        const {errorOpen, errorMsg} = this.state;

        return (
            <div>
                <Paper elevation={2}>
                    <Button variant="outlined" label="Resume" onClick={this.resume}>
                        Resume
                    </Button>
                    <Button variant="outlined" label="Resume" onClick={this.pause}>
                        Pause
                    </Button>
                    <Button variant="outlined" label="Resume" onClick={this.stop}>
                        Stop
                    </Button>
                </Paper>
                <Snackbar
                    open={errorOpen}
                    message={errorMsg}
                    autoHideDuration={2000}
                />
            </div>
        );
    }
}

class VideoListItem extends React.Component {
    play = () => {
        PlayVideo(this.props.video)
            .then(response => {
                console.log("Playing video");
                console.log(this.props.video);
            })
            .catch(error => {
                console.log("ERROR");
                console.log(error);
            });
    }

    render() {
        const {video} = this.props;

        return (
            <ListItem button onClick={this.play}>
                <ListItemText
                    primary={video.name + " " + video.length + "mb"}
                    secondary={video.path}/>
            </ListItem>
        );
    }
}

export default VideoPage;