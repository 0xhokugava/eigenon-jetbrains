/* OpenQASM 3.1 lexical highlighting fixture. */
OPENQASM 3.1;
include 'stdgates.inc';
pragma simulator ? `custom`

// Prepare and measure a Bell state.
qubit[2] q;
bit[2] result;
const float[64] theta = π / 2;
h q[0];
cx q[0], q[1];
rz(theta) q[1];
result = measure q;
