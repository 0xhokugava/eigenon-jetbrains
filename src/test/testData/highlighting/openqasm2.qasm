OPENQASM 2.0;
include "qelib1.inc";

// Prepare and measure a Bell state.
qreg q[2];
creg c[2];
gate bell a, b { h a; CX a, b; }
h q[0];
CX q[0], q[1];
rz(pi / 2) q[1];
measure q -> c;
