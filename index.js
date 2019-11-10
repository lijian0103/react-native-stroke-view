import React, {PureComponent} from 'react';
import {requireNativeComponent, View} from 'react-native';
import PropTypes from 'prop-types';

const RNStrokeView = requireNativeComponent("StrokeView", StrokeView);

export default class StrokeView extends PureComponent {

    static propTypes = {
        ...View.propTypes, //包含默认的View的属性，如果没有这句会报‘has no propType for native prop’错误

        onStrokeStop: PropTypes.func,
    };

    _onStrokeStop() {
        this.props.onStrokeStop && this.props.onStrokeStop()
    }

    render() {
        return <RNStrokeView  {...this.props} onStrokeStop={this._onStrokeStop.bind(this)}/>;
    }
}