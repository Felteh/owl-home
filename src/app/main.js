import React from 'react';
import {browserHistory} from 'react-router'
import {BottomNavigation, BottomNavigationItem} from 'material-ui/BottomNavigation';
import Paper from 'material-ui/Paper';
import FontIcon from 'material-ui/FontIcon';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';

const homeIcon = <FontIcon className="material-icons">home</FontIcon>;
const videoIcon = <FontIcon className="material-icons">ondemand_video</FontIcon>;
const lightsIcon = <FontIcon className="material-icons">lightbulb_outline</FontIcon>;

const videoPath = '/pages/video';
const lightPath = '/pages/light';

export class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
        this.calcSelectedIndex();
    }

    calcSelectedIndex() {
        var selectedIndex;
        switch (this.props.location.pathname) {
            case videoPath:
                selectedIndex = 0;
                break;
            case lightPath:
                selectedIndex = 1;
                break;
        }
        this.state.selectedIndex = selectedIndex;
    }

    goToVideo() {
        browserHistory.push('/pages/video');
        this.state.selectedIndex = 0;
    }

    goToLights() {
        browserHistory.push('/pages/light');
        this.state.selectedIndex = 1;
    }

    render() {
        return (
            <MuiThemeProvider>
                <div>
                    {this.props.children}
                    <Paper zDepth={1}>
                        <BottomNavigation selectedIndex={this.state.selectedIndex}>
                            <BottomNavigationItem
                                label="Video"
                                icon={videoIcon}
                                onTouchTap={()=> this.goToVideo()}
                            />
                            <BottomNavigationItem
                                label="Lights"
                                icon={lightsIcon}
                                onTouchTap={() => this.goToLights()}
                            />
                        </BottomNavigation>
                    </Paper>
                </div>
            </MuiThemeProvider>
        );
    }
}