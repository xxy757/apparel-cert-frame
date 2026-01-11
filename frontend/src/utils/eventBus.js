// A simple event bus using the mitt library, which is small and efficient.
// First, we need to add mitt to the project: npm install mitt
import mitt from 'mitt';

const emitter = mitt();

export default emitter;
