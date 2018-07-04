import React from 'react';
import {GetVideos, PauseVideo, PlayVideo, ResumeVideo, StopVideo} from "./VideoApi";
import Snackbar from "@material-ui/core/es/Snackbar/Snackbar";
import ListSubheader from "@material-ui/core/es/ListSubheader/ListSubheader";
import List from "@material-ui/core/es/List/List";
import LinearProgress from "@material-ui/core/es/LinearProgress/LinearProgress";
import ListItem from "@material-ui/core/es/ListItem/ListItem";
import ListItemText from "@material-ui/core/es/ListItemText/ListItemText";
import Grid from "@material-ui/core/es/Grid/Grid";
import PlayArrow from "@material-ui/icons/es/PlayArrow";
import Stop from "@material-ui/icons/es/Stop";
import Pause from "@material-ui/icons/es/Pause";
import IconButton from "@material-ui/core/es/IconButton/IconButton";

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

    onError = (error) => {
        this.setState({errorOpen: true, errorMsg: error.message});
    };

    errorClose = () => {
        this.setState({errorOpen: false});
    };

    render() {
        const {loading, videos, errorOpen, errorMsg} = this.state;

        let _this = this;
        return (
            <div>
                <VideoToolbar/>
                <List>
                    {loading && <LinearProgress/>}
                    {videos &&
                    videos.map(
                        function (v) {
                            return (
                                <VideoListItem
                                    key={v.path}
                                    video={v}
                                    onError={_this.onError}
                                />
                            );
                        }
                    )
                    }
                </List>
                <Snackbar
                    open={errorOpen}
                    onClose={this.errorClose}
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


    errorClose = () => {
        this.setState({errorOpen: false});
    };

    render() {
        const {errorOpen, errorMsg} = this.state;

        return (
            <div>
                <Grid container spacing={24} justify='space-around'>
                    <Grid item xs={4} style={{textAlign: 'center'}}>
                        <IconButton onClick={this.pause}>
                            <Pause style={{width: '48px', height: '48px'}}/>
                        </IconButton>
                    </Grid>
                    <Grid item xs={4} style={{textAlign: 'center'}}>
                        <IconButton onClick={this.resume}>
                            <PlayArrow style={{width: '48px', height: '48px'}}/>
                        </IconButton>
                    </Grid>
                    <Grid item xs={4} style={{textAlign: 'center'}}>
                        <IconButton onClick={this.stop}>
                            <Stop style={{width: '48px', height: '48px'}}/>
                        </IconButton>
                    </Grid>
                </Grid>
                <Snackbar
                    open={errorOpen}
                    message={errorMsg}
                    onClose={this.errorClose}
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
                this.props.onError(error);
                console.log("ERROR");
                console.log(error);
            });
    }

    render() {
        const {video} = this.props;

        return (
            <ListItem button onClick={this.play}>
                <ListItemText
                    primary={video.name}
                    secondary={video.length+"mb"}/>
            </ListItem>
        );
    }
}

export default VideoPage;