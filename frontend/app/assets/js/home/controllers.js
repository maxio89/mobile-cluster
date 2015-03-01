/**
 * Dashboard controllers.
 */
define([], function () {
    'use strict';

    var HomeCtrl = function ($scope) {

    };
    HomeCtrl.$inject = ['$scope'];

    var HeaderCtrl = function ($scope, $window) {
        $scope.reloadPage = function () {
            $window.location.reload();
        };
    };
    HeaderCtrl.$inject = ['$scope', '$window'];

    return {
        HeaderCtrl: HeaderCtrl,
        HomeCtrl: HomeCtrl
    };

});
