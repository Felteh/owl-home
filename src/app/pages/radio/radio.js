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
import Snackbar from 'material-ui/Snackbar';

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
    constructor(props) {
        super(props);
        this.state = {errorOpen: false, errorMsg: "No issue"};
    }
    stop() {
        var this_ = this;
        console.log("Requesting from /radio/stop");
        return fetch('/radio/stop').then(function (response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            console.log("SUCCESS");
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
            this_.setState({errorOpen: true, errorMsg: error.message});
        });
    }

    render() {
        return (
                <div>
                    <Toolbar>
                        <ToolbarGroup firstChild={true}>
                            <RaisedButton label="Stop" secondary={true} onTouchTap={() => this.stop()} />
                        </ToolbarGroup>
                    </Toolbar>
                    <Snackbar
                        open={this.state.errorOpen}
                        message={this.state.errorMsg}
                        autoHideDuration={2000}
                        />
                </div>
                );
    }
}

class RadioListing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            radio: [],
            loading: false,
            errorOpen: false,
            errorMsg: "No issue"
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
        var this_ = this;
        console.log("Requesting from /radio with query=" + query);
        fetch('/radio',
                {
                    method: "POST",
                    headers: new Headers({"Content-Type": "application/json"}),
                    body: JSON.stringify({query: query})
                })
                .then(function (response) {
                    if (!response.ok) {
                        throw Error(response.statusText);
                    }
                    return response.json();
                })
                .then(function (response) {
                    this_.setState({radio: response.slice(0, 20), loading: false, errorOpen: this_.state.errorOpen, errorMsg: this_.state.errorMsg});
                })
                .catch(function (error) {
                    console.log("EPIC FAIL ON QUERY");
                    console.error(error);
                    this_.setState({radio: [], loading: false, errorOpen: true, errorMsg: error.message});
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
                    <Snackbar
                        open={this.state.errorOpen}
                        message={this.state.errorMsg}
                        autoHideDuration={2000}
                        />
                </div>
                            );
            }
}


class RadioListItem extends React.Component {
    constructor(props) {
        super(props);
        this.state = {Listeners: 0};
        if (props.radio && props.radio.Listeners) {
            var l = props.radio.Listeners;
            if (l > 1000) {
                l = Math.round(l / 1000) + "k";
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