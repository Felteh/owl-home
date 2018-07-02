import React from 'react';
import {browserHistory} from 'react-router'
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';

const homeIcon = <FontIcon className="material-icons">home</FontIcon>;
const videoIcon = <FontIcon className="material-icons">ondemand_video</FontIcon>;
const lightsIcon = <FontIcon className="material-icons">lightbulb_outline</FontIcon>;

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
            case radioPath:
                selectedIndex = 1;
                break;
            case videoPath:
                selectedIndex = 2;
                break;
            case lightPath:
                selectedIndex = 3;
                break;
        }
        this.state.selectedIndex = selectedIndex;
    }
    static goToHome() {
        browserHistory.push('/');
    }
    static goToRadio() {
        browserHistory.push(radioPath);
    }
    static goToVideo() {
        browserHistory.push(videoPath);
    }
    static goToLights() {
        browserHistory.push(lightPath);
    }

    render() {
        return (
                <MuiThemeProvider>
                    <div>
                        <Tabs initialSelectedIndex={this.state.selectedIndex}>
                            <Tab
                                icon={homeIcon}
                                label="Home"
                                onActive={() => this.goToHome()}
                                />
                            <Tab
                                icon={radioIcon}
                                label="Radio"
                                onActive={() => this.goToRadio()}
                                />
                            <Tab
                                icon={videoIcon}
                                label="Video"
                                onActive={() => this.goToVideo()}
                                />
                            <Tab
                                icon={lightsIcon}
                                label="Lights"
                                onActive={() => this.goToLights()}
                                />
                        </Tabs>
                        <Paper zDepth={1}>
                            {this.props.children}
                        </Paper>
                    </div>
                </MuiThemeProvider>
                );
    }
}