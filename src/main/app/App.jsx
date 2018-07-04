import React from 'react';
import ReactDom from 'react-dom';
import {browserHistory, BrowserRouter, Route, Switch} from "react-router-dom";
import Paper from "@material-ui/core/es/Paper/Paper";
import HomePage from "./pages/HomePage";
import VideoPage from "./pages/video/VideoPage";
import NavMenu from "./NavMenu";

const Blah = () => (
    <div>Hey</div>
);

const Body = () => (
    <Paper elevation={1}>
        <div style={{padding: '16px'}}>
            <Switch>
                <Route exact path='/' component={HomePage}/>
                <Route path='/pages/video' component={VideoPage}/>
                <Route path='/pages/light' component={Blah}/>
            </Switch>
        </div>
    </Paper>
);

const App = () => (
    <div>
        <NavMenu/>
        <Body/>
    </div>
);

ReactDom.render(
    <BrowserRouter>
        <App/>
    </BrowserRouter>,
    document.getElementById("app")
);