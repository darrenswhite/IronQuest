import './index';
import Actions from '../../components/Actions.vue';
import Settings from '../../components/Settings.vue';
import { Action, Path, QuestAccessFilter, QuestTypeFilter } from 'ironquest';
import { Cache, PathFinder } from '../../lib';
import Vue from 'vue';

$(() => {
  Cache.getInstance().loadParameters();

  const vm = new Vue({
    data: {
      actions: {
        bus: new Vue(),
        error: false,
        loading: false,
        path: null as Path | null,
        selectedAction: null as Action | null | undefined,
      },
      parameters: {
        name: PathFinder.getInstance().parameters.name,
        typeFilter:
          PathFinder.getInstance().parameters.typeFilter || QuestTypeFilter.ALL,
        accessFilter:
          PathFinder.getInstance().parameters.accessFilter ||
          QuestAccessFilter.ALL,
        ironman: PathFinder.getInstance().parameters.ironman,
        recommended: PathFinder.getInstance().parameters.recommended,
        lampSkills: PathFinder.getInstance().parameters.lampSkills || [],
      },
    },
    methods: {
      run(): void {
        this.updateParameters();
        PathFinder.getInstance().find();
      },
      updateParameters(): void {
        PathFinder.getInstance().parameters = {
          name: this.parameters.name,
          typeFilter: this.parameters.typeFilter,
          accessFilter: this.parameters.accessFilter,
          ironman: this.parameters.ironman,
          recommended: this.parameters.recommended,
          lampSkills: this.parameters.lampSkills,
        };
      },
      showLoader(): void {
        vm.actions.bus.$emit('showLoader');
      },
      displayActionsSuccess(path: Path): void {
        vm.actions.bus.$emit('displayActionsSuccess', path);
      },
      displayActionsFailure(response: unknown): void {
        vm.actions.bus.$emit('displayActionsFailure', response);
      },
    },
    components: {
      Actions,
      Settings,
    },
  }).$mount('#app');

  $(window).on('beforeunload', () => {
    vm.updateParameters();
    Cache.getInstance().saveParameters();
  });

  PathFinder.getInstance().listeners.push({
    start: vm.showLoader,
    success: vm.displayActionsSuccess,
    failure: vm.displayActionsFailure,
  });
});