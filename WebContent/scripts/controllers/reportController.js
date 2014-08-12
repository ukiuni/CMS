myApp.controller("reportController", [ "$rootScope", "$scope", "$http", "$location", "$modal", function($rootScope, $scope, $http, $location, $modal) {
	if (!$location.search()["key"]) {
		return;
	}
	var reportLoadAccessKey = null;
	if ($rootScope.loginAccount) {
		reportLoadAccessKey = $rootScope.loginAccount.accessKey;
	}
	$http.get("api/report/load/" + $location.search()["key"], {
		params : {
			accountAccessKey : reportLoadAccessKey
		}
	}).success(function(report) {
		$scope.report = report;
		$http.get("api/report/" + report.key + "/comment", {
			params : {
				accountAccessKey : reportLoadAccessKey
			}
		}).success(function(comments) {
			$scope.comments = comments;
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.loadComment'));
		});
	}).error(function(e) {
		$rootScope.showAlert($translate.instant('error.loadReport'));
		$location.path('/');
	});
	var switchFold = function(fold) {
		if (!$rootScope.loginAccount) {
			$location.search('afterLoginPath', $location.path())
			$location.path('/login');
			return;
		}
		if ($scope.folding) {
			return;
		}
		$scope.folding = true;
		var onSuccessFunction = function() {
			$scope.report.folded = fold;
			if (fold) {
				$scope.report.foldedCount++;
			} else {
				$scope.report.foldedCount--;
			}
			$scope.folding = false;
		}
		var onErrorFunction = function() {
			$scope.folding = false;
		}
		var params = {
			key : $scope.report.key,
			accountAccessKey : $rootScope.loginAccount.accessKey
		}
		if (fold) {
			$http.post("api/report/fold", params).success(onSuccessFunction).error(onErrorFunction);
		} else {
			$http["delete"]("api/report/fold", {
				params : params
			}).success(onSuccessFunction).error(onErrorFunction);
		}
	}
	$scope.fold = function() {
		switchFold(true);
	}
	$scope.unfold = function() {
		switchFold(false);
	}
	$scope.follow = function() {
		$http.post("api/account/follow", {
			accountAccessKey : $rootScope.loginAccount.accessKey,
			targetAccountId : $scope.report.reporter.id,
		}).success(function() {
			$scope.report.reporter.following = true;
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.follow'));
		});
	}
	$scope.unfollow = function() {
		$http.put("api/account/unfollow", {
			accountAccessKey : $rootScope.loginAccount.accessKey,
			targetAccountId : $scope.report.reporter.id,
		}).success(function() {
			$scope.report.reporter.following = false;
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.unfollow'));
		});
	}
	$scope.createAccountForComment = function() {
		$location.search('afterLoginPath', $location.path())
		$location.path('/login');
	}
	$scope.comment = function() {
		if ("" == $scope.inputMessage) {
			return;
		}
		$http.post("api/report/comment", {
			accountAccessKey : $rootScope.loginAccount.accessKey,
			reportKey : $scope.report.key,
			message : $scope.inputMessage
		}).success(function(comments) {
			$scope.inputMessage = "";
			$scope.comments = comments;
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.saveComment'));
		});
	}
	$scope.deleteComment = function(comment) {
		$http["delete"]("api/report/" + $scope.report.key + "/comment", {
			params : {
				accountAccessKey : $rootScope.loginAccount.accessKey,
				commentId : comment.id
			}
		}).success(function(comments) {
			$scope.comments = comments
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.deleteComment'));
		});
	}
	$scope.updateComment = function(comment) {
		$http.put("api/report/" + $scope.report.key + "/comment", {
			accountAccessKey : $rootScope.loginAccount.accessKey,
			id : comment.id,
			message : comment.message
		}).success(function(comments) {
			$scope.comments = comments
		}).error(function(e) {
			$rootScope.showAlert($translate.instant('error.saveComment'));
		});
	}
	$scope.openDeleteCommentDialog = function(comment) {
		var ModalInstanceCtrl = [ "$scope", "$modalInstance", "comment", function($scope, $modalInstance, comment) {
			$scope.title = "delete comment?";
			$scope.body = "Are you sure to delete comment? \n \"" + comment.message + "\"";
			$scope.execute = function() {
				$modalInstance.close(comment);
			};
		} ];
		var modalInstance = $modal.open({
			templateUrl : 'template/confirmDialog.html?' + new Date().getTime(),
			controller : ModalInstanceCtrl,
			resolve : {
				comment : function() {
					return comment;
				}
			}
		});
		modalInstance.result.then(function(comment) {
			$scope.deleteComment(comment);
		}, function() {
			//console.log('Modal dismissed at: ' + new Date());
		});
	}
	$scope.openEditCommentDialog = function(comment) {
		var ModalInstanceCtrl = [ "$scope", "$modalInstance", "comment", function($scope, $modalInstance, comment) {
			$scope.comment = comment;
			$scope.execute = function(comment) {
				$modalInstance.close(comment);
			};
		} ];
		var modalInstance = $modal.open({
			templateUrl : 'template/editCommentDialog.html?' + new Date().getTime(),
			controller : ModalInstanceCtrl,
			resolve : {
				comment : function() {
					return $rootScope.clone(comment);
				}
			}
		});
		modalInstance.result.then(function(comment) {
			$scope.updateComment(comment);
		}, function() {
			//console.log('Modal dismissed at: ' + new Date());
		});

	}
} ]);
