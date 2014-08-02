myApp.controller("logoutController", [ "$rootScope", "$location", "$cookies", function($rootScope, $location, $cookies) {
	$rootScope.loginAccount = null;
	var past = new Date();
	past.setTime(0);
	document.cookie = "accountAccessKey=;expires=" + past.toGMTString();
	for ( var i in $location.search()) {
		$location.search(i, null)
	}
	$location.path('/');
} ]);