import React from 'react';
import {browserHistory} from 'react-router-dom'
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Paper from "@material-ui/core/es/Paper/Paper";
import Home from "@material-ui/icons/es/Home";
import VideoLibrary from "@material-ui/icons/es/VideoLibrary";
import Highlight from "@material-ui/icons/es/Highlight";

const videoPath = '/pages/video';
const lightPath = '/pages/light';

export class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {selectedIndex: 0};
        this.calcSelectedIndex();
    }

    calcSelectedIndex() {
        let selectedIndex = 0;
        switch (this.props.location.pathname) {
            case videoPath:
                selectedIndex = 1;
                break;
            case lightPath:
                selectedIndex = 2;
                break;
        }
        this.state.selectedIndex = selectedIndex;
    }

    static goToHome() {
        browserHistory.push('/');
    }

    static goToVideo() {
        browserHistory.push(videoPath);
    }

    static goToLights() {
        browserHistory.push(lightPath);
    }

    render() {
        return (
            <div>
                <Tabs initialSelectedIndex={this.state.selectedIndex}>
                    <Tab
                        icon={<Home/>}
                        label="Home"
                        onActive={() => this.goToHome()}
                    />
                    <Tab
                        icon={<VideoLibrary/>}
                        label="Video"
                        onActive={() => this.goToVideo()}
                    />
                    <Tab
                        icon={<Highlight/>}
                        label="Lights"
                        onActive={() => this.goToLights()}
                    />
                </Tabs>
                <Paper zDepth={1}>
                    {this.props.children}
                </Paper>
            </div>
        );
    }
}