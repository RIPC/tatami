TagsModule.controller('TagsController', [
    '$scope',
    '$resource',
    '$location',
    '$resource',
    'TagService',
    'SearchService', 
    function($scope, $resource, $location, $resource, TagService, SearchService) {
        $scope.current = {
            searchString: ''
        };
        /**
         * Initialization function. Gets the tags immediately, this may be something can be resolved in routing
         * @returns {*}
         */
        $scope.getTags = function() {
            // Factor into a service
            var promise = $resource('/tatami/rest/tags/popular').query(function(result) {
                $scope.tags = result;
            });
            return promise;
        };

        /**
         * Perhaps this isn't the best way, but when we switch tabs, the view stays the same, except the model data
         * changes. The routing is set up in such a way that the url for the new data is made available to the controller
         * through toState.data.dataUrl. Now we can perform a query on this url.
         */
        $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParam) {
            if($scope.current.searchString != '') {
                SearchService.query({ term: 'tags', q: $scope.current.searchString }, function(result) {
                    $scope.tags = result;
                });
            }
            else if(toState.data.dataUrl) {
                $resource(toState.data.dataUrl).query(function(result) {
                    $scope.tags = result;
                });
            }
            else {
                $scope.tags = {};
            }
        });

        $scope.isActive = function (path) {
            return path === $location.path();
        };

        /**
         * Follows an unfollowed tag, or unfollows a followed tag, depending on the current state
         * @param tag
         */

        $scope.followTag = function(tag, index) {
            TagService.follow(
                { tag: tag.name },
                { name: tag.name, followed: !tag.followed, trendingUp: tag.trendingUp },
                function(response) {
                    $scope.tags[index].followed = response.followed;
            });
        };

        $scope.contains = function(path) {
            return $location.path().contains(path);
        };

        $scope.search = function() {
            // Update the route
            $scope.$state.go('account.tags.search', { q: $scope.current.searchString });
        }
    }
]);