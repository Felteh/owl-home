import React from 'react';
import BottomNavigation from '@material-ui/core/BottomNavigation';
import BottomNavigationAction from '@material-ui/core/BottomNavigationAction';
import VideoLibrary from "@material-ui/icons/es/VideoLibrary";
import Highlight from "@material-ui/icons/es/Highlight";
import Home from "@material-ui/icons/es/Home";
import {Link} from "react-router-dom";

class BottomMenu extends React.Component {
    state = {
        value: 0,
    };

    handleChange = (event, value) => {
        this.setState({value});
    };

    render() {
        const {value} = this.state;

        return (
            <BottomNavigation
                value={value}
                onChange={this.handleChange}
                showLabels
            >
                <BottomNavigationAction label="Home" icon={<Home/>} component={Link} to="/"/>
                <BottomNavigationAction label="Video" icon={<VideoLibrary/>} component={Link} to="/pages/video"/>
                <BottomNavigationAction label="Lights" icon={<Highlight/>} component={Link} to="/pages/light"/>
            </BottomNavigation>
        );
    }
}

export default BottomMenu;