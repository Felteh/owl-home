import React from 'react';
import List from "@material-ui/core/es/List/List";
import ListSubheader from "@material-ui/core/es/ListSubheader/ListSubheader";
import ListItem from "@material-ui/core/es/ListItem/ListItem";
import IconButton from "@material-ui/core/es/IconButton/IconButton";

export class LightListingPage extends React.Component {
    render() {
        return (
            <div>
                <LightListing/>
            </div>
        );
    }
}

class LightListing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {lights: []};
    }

    componentDidMount() {
        console.log("Requesting from /lights");
        var _this = this;
        fetch('/lights')
            .then(function (response) {
                return response.json()
            })
            .then(function (response) {
                _this.setState({lights: response});
            })
            .catch(function (error) {
                console.log("EPIC FAIL ON QUERY");
                console.error(error);
            });
    }

    render() {
        return (
            <List>
                <ListSubheader>Lights</ListSubheader>
                {
                    this.state.lights.map(
                        function (v) {
                            return (
                                <LightListItem
                                    key={v.id}
                                    light={v}
                                />
                            );
                        })
                }
            </List>
        );
    }
}

class LightListItem extends React.Component {
    render() {
        return (
            <ListItem
                leftIcon={(<IconButton className="material-icons">{this.props.light.icon}</IconButton>)}
                primaryText={this.props.light.name}
                primaryTogglesNestedList={true}
                initiallyOpen={this.props.light.enabled}
                nestedItems=
                    {
                        [
                            <OnOffToggle key="1" light={this.props.light}/>,
                            <ListItem
                                key="2"
                                primaryText="White (Not Working)"
                            />,
                            <ListItem
                                key="3"
                                primaryText="Red (Not Working)"
                            />,
                            <ListItem
                                key="4"
                                primaryText="Blue (Not Working)"
                            />
                        ]
                    }
            />
        );
    }
}

class OnOffToggle extends React.Component {
    constructor(props) {
        super(props);
        this.state = {toggled: props.light.enabled};
    }

    on() {
        var _this = this;
        return fetch('/lights', {
            method: "POST",
            headers: new Headers({"Content-Type": "application/json"}),
            body: JSON.stringify({lightAddress: this.props.light.id, state: "on"})
        }).then(function (response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            console.log("SUCCESS");
            _this.setState({toggled: true});
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
        });
    }

    off() {
        var _this = this;
        return fetch('/lights', {
            method: "POST",
            headers: new Headers({"Content-Type": "application/json"}),
            body: JSON.stringify({lightAddress: this.props.light.id, state: "off"})
        }).then(function (response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            console.log("SUCCESS");
            _this.setState({toggled: false});
        }).catch(function (error) {
            console.log("FAIL");
            console.error(error);
        });
    }

    onToggle() {
        var newState = !this.state.toggled;
        if (newState) {
            this.on();
        } else {
            this.off();
        }
    }

    render() {
        return (
            <ListItem
                key="1"
                primaryText="Enabled"
                rightToggle={
                    <Toggle
                        toggled={this.state.toggled}
                        onToggle={this.onToggle.bind(this)}
                    />
                }
            />
        );
    }
}