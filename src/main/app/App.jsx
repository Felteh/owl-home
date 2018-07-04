import React from 'react';
import ReactDom from 'react-dom';
import {browserHistory, BrowserRouter, Route, Switch} from "react-router-dom";
import HomePage from "./pages/HomePage";
import VideoPage from "./pages/video/VideoPage";
import NavMenu from "./NavMenu";

const Blah = () => (
    <div>Hey</div>
);

const Body = () => (
    <div style={{marginTop: '16px'}}>
        <Switch>
            <Route exact path='/' component={HomePage}/>
            <Route path='/pages/video' component={VideoPage}/>
            <Route path='/pages/light' component={Blah}/>
        </Switch>
    </div>
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