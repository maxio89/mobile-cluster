/**
 * Dashboard routes.
 */
define(['angular', './controllers', 'common'], function (angular, controllers) {
    'use strict';

    var mod = angular.module('home.routes', ['pl.agh.edu.common']);
    mod.config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/', {templateUrl: '/assets/partials/index.html', controller: controllers.HomeCtrl});
    }]);
    return mod;
});
