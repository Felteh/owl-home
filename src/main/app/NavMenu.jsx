import React from 'react';
import BottomNavigation from '@material-ui/core/BottomNavigation';
import BottomNavigationAction from '@material-ui/core/BottomNavigationAction';
import VideoLibrary from "@material-ui/icons/es/VideoLibrary";
import Highlight from "@material-ui/icons/es/Highlight";
import Home from "@material-ui/icons/es/Home";
import {Link} from "react-router-dom";
import IconButton from "@material-ui/core/es/IconButton/IconButton";
import Menu from "@material-ui/icons/es/Menu";
import Typography from "@material-ui/core/es/Typography/Typography";
import Toolbar from "@material-ui/core/es/Toolbar/Toolbar";
import AppBar from "@material-ui/core/es/AppBar/AppBar";
import Drawer from "@material-ui/core/es/Drawer/Drawer";
import List from "@material-ui/core/es/List/List";
import ListItem from "@material-ui/core/es/ListItem/ListItem";
import ListItemText from "@material-ui/core/es/ListItemText/ListItemText";
import ListSubheader from "@material-ui/core/es/ListSubheader/ListSubheader";

class NavMenu extends React.Component {
    state = {
        value: 0,
    };

    menuOpen = () => {
        this.setState({open: true});
    };

    menuClose = () => {
        this.setState({open: false});
    };

    render() {
        const {value} = this.state;

        return (
            <AppBar position="static">
                <Toolbar>
                    <IconButton onClick={this.menuOpen}>
                        <Menu/>
                    </IconButton>
                    <Typography variant="title" color="inherit" style={{paddingLeft: "16px", color: "white"}}>
                        Owly Home
                    </Typography>
                </Toolbar>

                <Drawer open={this.state.open} onClose={this.menuClose}>
                    <div
                        tabIndex={0}
                        role="button"
                        onClick={this.menuClose}
                    >
                        <List subheader={<ListSubheader>Pages</ListSubheader>}>
                            <ListItem button component={Link} to="/">
                                <ListItemText primary="Home"/>
                            </ListItem>
                            <ListItem button component={Link} to="/pages/video">
                                <ListItemText primary="Video"/>
                            </ListItem>
                            <ListItem button component={Link} to="/pages/light">
                                <ListItemText primary="Lights"/>
                            </ListItem>
                        </List>
                    </div>
                </Drawer>
            </AppBar>
        );
    }
}

export default NavMenu;