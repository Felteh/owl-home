import React from 'react';
import {browserHistory} from 'react-router'
        import {BottomNavigation, BottomNavigationItem} from 'material-ui/BottomNavigation';
import Paper from 'material-ui/Paper';
import FontIcon from 'material-ui/FontIcon';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import {Tabs, Tab} from 'material-ui/Tabs';

const homeIcon = <FontIcon className="material-icons">home</FontIcon>;
const radioIcon = <FontIcon className="material-icons">radio</FontIcon>;
const videoIcon = <FontIcon className="material-icons">ondemand_video</FontIcon>;
const lightsIcon = <FontIcon className="material-icons">lightbulb_outline</FontIcon>;

const radioPath = '/pages/radio';
const videoPath = '/pages/video';
const lightPath = '/pages/light';

export class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {selectedIndex: 0};
        this.calcSelectedIndex();
    }

    calcSelectedIndex() {
        var selectedIndex = 0;
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
    goToHome() {
        browserHistory.push('/');
    }
    goToRadio() {
        browserHistory.push(radioPath);
    }
    goToVideo() {
        browserHistory.push(videoPath);
    }

    goToLights() {
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