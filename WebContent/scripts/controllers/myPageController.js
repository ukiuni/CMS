myApp.controller("myPageController", [ "$rootScope", "$scope", "$http", "$location", function($rootScope, $scope, $http, $location) {
	if (!$rootScope.loginAccount) {
		$location.path('/');
		return;
	}
	$http.get("api/report/loadByAccessKey", {
		params : {
			accessKey : $rootScope.loginAccount.accessKey
		}
	}).success(function(data) {
		$scope.reports = data;
	}).error(function(e) {
		console.log("error" + e);
	});
	$http.get("api/account/fold", {
		params : {
			accessKey : $rootScope.loginAccount.accessKey
		}
	}).success(function(data) {
		$scope.folds = data;
	}).error(function(e) {
		console.log("error" + e);
	});
	$http.get("api/news", {
		params : {
			accessKey : $rootScope.loginAccount.accessKey
		}
	}).success(function(data) {
		$scope.newses = data;
	}).error(function(e) {
		console.log("error" + e);
	});
	$scope.createReport = function() {
		for ( var i in $location.search()) {
			$location.search(i, null)
		}
		$location.path('/editReport');
	}
	$scope.editProfile = function() {
		$location.path('/editProfile');
	}
	$scope.showReport = function(reportKey) {
		$location.search('key', reportKey)
		$location.path('/editReport');
	}
	$scope.showPublishedReport = function(report, $event) {
		$location.search("key", report.key).path("/report");
		if ($event) {
			$event.stopPropagation();
		}
	}
} ]);
