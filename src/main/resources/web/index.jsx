var Route = ReactRouter.Route;

ReactDOM.render(
    <ReactRouter.Router history={History.createHistory()}>
        <ReactRouter.Route path="/signup" component={SignUpPage}/>
        <ReactRouter.Route path="/" component={App}>
            <ReactRouter.IndexRoute component={ActivityStream}/>
            <ReactRouter.Route path="/users/:userId" component={UserChirps}/>
            <ReactRouter.Route path="/addFriend" component={AddFriendPage}/>
        </Route>
    </ReactRouter.Router>,
    document.getElementById("content")
);
