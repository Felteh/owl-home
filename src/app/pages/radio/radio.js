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
import TextField from 'material-ui/TextField';
import LinearProgress from 'material-ui/LinearProgress';
import Badge from 'material-ui/Badge';

export class RadioListingPage extends React.Component {
    render() {
        return (
                <div>
                    <RadioToolbar/>
                    <RadioListing/>
                </div>
                );
    }
}

class RadioToolbar extends React.Component {

    stop() {
        console.log("Requesting from /radio/stop");
        return fetch('/radio/stop').then(function (response) {
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
        });
    }

    render() {
        return (
                <Toolbar>
                    <ToolbarGroup firstChild={true}>
                        <RaisedButton label="Stop" secondary={true} onTouchTap={() => this.stop()} />
                    </ToolbarGroup>
                </Toolbar>
                );
    }
}

class RadioListing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            radio: [],
            loading: false
        };
    }
    componentDidMount() {
        this.loadStations();
    }
    loadStations() {
        var query = "";
        if (this.refs.stationSearch && this.refs.stationSearch.getValue()) {
            query = this.refs.stationSearch.getValue();
        }
        this.setState({radio: this.state.radio, loading: true});
        var _this = this;
        console.log("Requesting from /radio with query=" + query);
        fetch('/radio',
                {
                    method: "POST",
                    headers: new Headers({"Content-Type": "application/json"}),
                    body: JSON.stringify({query: query})
                })
                .then(function (response) {
                    return response.json();
                })
                .then(function (response) {
                    _this.setState({radio: response.slice(0,20), loading: false});
                })
                .catch(function (error) {
                    console.log("EPIC FAIL ON QUERY");
                    console.error(error);
                    _this.setState({radio: [], loading: false});
                });
    }

    render() {
        var loading;
        if (this.state.loading) {
            loading = <LinearProgress mode="indeterminate" />;
        }
        return (
                <div>
                    <span>            
                        <TextField
                            fullWidth={true}
                            hintText="Christmas/Pop/Jazz"
                            floatingLabelText="Search Stations"
                            ref="stationSearch"
                            />
                        <RaisedButton label="Submit" primary={true} onTouchTap={() => this.loadStations()}/>
                    </span>
                    <List>
                    {loading}
                    <Subheader>Stations</Subheader>
                    {
                        this.state.radio.map(
                                function (r) {
                                    return (
                                                <RadioListItem
                                                    key={r.ID}
                                                    radio={r}
                                                    />
                                                );
                    })
                    }
                    </List>
                </div>
                            );
            }
}


class RadioListItem extends React.Component {
    constructor(props) {
        super(props);
        this.state= {Listeners:0};
        if (props.radio && props.radio.Listeners) {
            var l = props.radio.Listeners;
            if(l>1000){
                l=Math.round(l/1000)+"k";
            }
            this.state = {Listeners: l};
        }
    }
    play(radio) {
        console.log("Requesting from /radio/play");
        return fetch('/radio/play', {
            method: "POST",
            headers: new Headers({"Content-Type": "application/json"}),
            body: JSON.stringify({station: radio.ID})
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
                    onTouchTap={() => this.play(this.props.radio)}
                    primaryText={this.props.radio.Name}
                    secondaryText={this.props.radio.Genre}
                    rightIcon ={(
                                <Badge
                                    badgeContent={this.state.Listeners}
                                    primary={true}
                                    >
                                    <FontIcon className="material-icons">headset</FontIcon>
                                </Badge>
                        )} 
                    />
                );
    }
}