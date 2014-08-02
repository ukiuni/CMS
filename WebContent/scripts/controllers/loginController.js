myApp.controller("loginController", [ "$rootScope", "$scope", "$http", "$location", "$cookies", "$translate", function($rootScope, $scope, $http, $location, $cookies, $translate) {
	$scope.tryLoginAccount = {};
	$scope.newAccount = {};
	if ($rootScope.loginAccount) {
		$location.path('/myPage');
		return;
	}
	var routeToAfterLogin = function() {
		if ($location.search()["afterLoginPath"]) {
			$location.path($location.search()["afterLoginPath"]);
			$location.search("afterLoginPath", null);
		} else {
			$location.path('/myPage');
		}
	}
	$scope.registCookie = function() {
		var cookieExpires = new Date();
		cookieExpires.setTime(cookieExpires.getTime() + 30 * 24 * 60 * 60 * 1000);
		document.cookie = "accountAccessKey=" + $rootScope.loginAccount.accessKey + ";expires=" + cookieExpires.toGMTString() + ";";
	}
	$scope.createAccount = function() {
		$http.post("api/account", $scope.newAccount).success(function(data) {
			$rootScope.loginAccount = data;
			$scope.registCookie();
			routeToAfterLogin();
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.createAccount'));
		});
	}
	$scope.login = function() {
		$http.post("api/account/login", $scope.tryLoginAccount).success(function(data) {
			$rootScope.loginAccount = data;
			$scope.registCookie();
			routeToAfterLogin();
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.login'));
		});
	}
	$scope.action = "save";
} ]);