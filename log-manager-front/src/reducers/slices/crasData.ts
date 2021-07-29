import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { CrasDataCreateOption, CrasDataJudgeOption } from '../../types/crasData';
import { RootState } from '../rootReducer';

interface CrasDataCreateAllOption extends CrasDataCreateOption {
  targetTable: string[];
  columnTable: string[];
}
export interface CrasDataOptions {
  create: CrasDataCreateAllOption;
  judge: CrasDataJudgeOption;
}
export interface CrasState {
  showAdd: boolean;

  selectType: 'create' | 'judge' | undefined;
  selectSiteId: number | undefined;
  selectItemId: number | undefined;
  selectTable: string | undefined;
  drawerType: 'add' | 'edit' | undefined;
  showCreateDrawer: boolean;
  showJudgeDrawer: boolean;
  options: CrasDataOptions;
}

const initialState: CrasState = {
  showAdd: false,
  selectType: undefined,
  selectSiteId: undefined,
  selectItemId: undefined,
  selectTable: undefined,
  drawerType: undefined,
  showCreateDrawer: false,
  showJudgeDrawer: false,
  options: {
    create: {
      calPeriodUnit: [],
      calResultType: [],
      operations: [],
      targetTable: [],
      columnTable: [],
    },
    judge: {
      compare: [],
      condition: [],
    },
  },
};

const crasData = createSlice({
  name: 'crasData',
  initialState,
  reducers: {
    initCrasReducer: () => initialState,
    setCrasAddVisibleReducer: (state, action: PayloadAction<boolean>) => {
      state.showAdd = action.payload;
    },
    setCrasTypeReducer: (state, action: PayloadAction<'create' | 'judge' | undefined>) => {
      state.selectType = action.payload;
    },
    setCrasSiteIdReducer: (state, action: PayloadAction<number | undefined>) => {
      state.selectSiteId = action.payload;
    },
    setCrasItemIdReducer: (state, action: PayloadAction<number | undefined>) => {
      state.selectItemId = action.payload;
    },
    setCrasDrawerTypeReducer: (state, action: PayloadAction<'add' | 'edit' | undefined>) => {
      state.drawerType = action.payload;
    },
    setCrasShowCreateDrawerReducer: (state, action: PayloadAction<boolean>) => {
      if (action.payload) {
        state.showJudgeDrawer = false;
      } else {
        state.drawerType = undefined;
        state.selectItemId = undefined;
        state.options.create.targetTable = [];
        state.options.create.columnTable = [];
      }
      state.showCreateDrawer = action.payload;
    },
    setCrasShowJudgeDrawerReducer: (state, action: PayloadAction<boolean>) => {
      if (action.payload) {
        state.showCreateDrawer = false;
      } else {
        state.drawerType = undefined;
        state.selectItemId = undefined;
      }
      state.showJudgeDrawer = action.payload;
    },
    setCrasCreateTargetTableOption: (state, action: PayloadAction<string[]>) => {
      state.options.create.targetTable = action.payload;
    },
    setCrasCreateColumnTableOption: (state, action: PayloadAction<string[]>) => {
      state.options.create.columnTable = action.payload;
    },
    setCrasCreateFixedOption: (state, action: PayloadAction<CrasDataCreateOption>) => {
      state.options.create.operations = action.payload.operations;
      state.options.create.calPeriodUnit = action.payload.calPeriodUnit;
      state.options.create.calResultType = action.payload.calResultType;
    },
    setCrasJudgeFixedOption: (state, action: PayloadAction<CrasDataJudgeOption>) => {
      state.options.judge.compare = action.payload.compare;
      state.options.judge.condition = action.payload.condition;
    },
    setCrasCreateSelectTable: (state, action: PayloadAction<string>) => {
      state.selectTable = action.payload;
    },
  },
});

export const {
  initCrasReducer,
  setCrasAddVisibleReducer,
  setCrasTypeReducer,
  setCrasSiteIdReducer,
  setCrasItemIdReducer,
  setCrasDrawerTypeReducer,
  setCrasShowCreateDrawerReducer,
  setCrasShowJudgeDrawerReducer,
  setCrasCreateTargetTableOption,
  setCrasCreateColumnTableOption,
  setCrasCreateFixedOption,
  setCrasJudgeFixedOption,
  setCrasCreateSelectTable,
} = crasData.actions;

export const crasShowAddSelector = (state: RootState): boolean => state.crasData.showAdd;
export const crasTypeSelector = (state: RootState): 'create' | 'judge' | undefined => state.crasData.selectType;
export const crasSiteIdSelector = (state: RootState): number | undefined => state.crasData.selectSiteId;
export const crasItemIdSelector = (state: RootState): number | undefined => state.crasData.selectItemId;
export const crasDrawerTypeSelector = (state: RootState): 'add' | 'edit' | undefined => state.crasData.drawerType;
export const crasShowCreateDrawerSelector = (state: RootState): boolean => state.crasData.showCreateDrawer;
export const crasShowJudgeDrawerSelector = (state: RootState): boolean => state.crasData.showJudgeDrawer;
export const crasCreateOptionSelector = (state: RootState): CrasDataCreateAllOption | undefined =>
  state.crasData.options.create;
export const crasJudgeOptionSelector = (state: RootState): CrasDataJudgeOption | undefined =>
  state.crasData.options.judge;
export const crasCreateTableSelector = (state: RootState): string | undefined => state.crasData.selectTable;

export default crasData.reducer;
