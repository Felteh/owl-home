var App = React.createClass({
    getInitialState: function () {
        return {};
    },
    render: function () {
        return (
 <div id="page-content">
            <h1>yo</h1>
            {this.props.children}
</div>
        );
    }
});

var OptionsMenu = React.createClass({
    getInitialState: function () {
        return {};
    },
    render: function () {
        return (
            <h1>HELLO</h1>
        );
    }
});

ReactDOM.render(
    <ReactRouter.Router history={ReactRouter.browserHistory}>
        <ReactRouter.Route path="/" component={App}>
            <ReactRouter.IndexRoute component={OptionsMenu}/>
        </ReactRouter.Route>
    </ReactRouter.Router>,
    document.getElementById("content")
);
