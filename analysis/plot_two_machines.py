"""
Two-machine performance comparison charts (Machine A vs Machine B).

Generates the Machine A vs Machine B comparison figures used in the performance
analysis. Machine A = Intel Core i5-8265U (4C/8T), Machine B = Intel Core i5-10300H
(4C/8T). Values are the measured benchmark results from both laptops.

Usage:  python analysis/plot_two_machines.py   ->   writes results/cmp_*.png
"""
import os
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

R = "results"
os.makedirs(R, exist_ok=True)

# ---- measured data ----
threads = [1, 2, 4, 8]
avgA_sp = [1.00, 1.91, 3.45, 5.46]
avgB_sp = [1.00, 1.85, 2.93, 3.39]
avgA_ef = [1.0000, 0.9560, 0.8633, 0.6826]
avgB_ef = [1.0000, 0.9245, 0.7315, 0.4240]

FILTERS = ["Gaussian 5x5", "Sobel 3x3", "Sharpen 3x3", "Box Blur 5x5"]

# execution times (ms) on the 8192x8192 image, threads = 1,2,4,8
A8192 = {
    "Gaussian 5x5": [12565.763, 6114.581, 3442.287, 2131.577],
    "Sobel 3x3":    [6442.484, 3493.382, 2085.753, 1375.969],
    "Sharpen 3x3":  [4734.202, 2475.791, 1443.502, 930.618],
    "Box Blur 5x5": [12071.988, 6229.966, 3523.160, 2189.088],
}
B8192 = {
    "Gaussian 5x5": [5073.260, 2728.007, 1672.623, 1534.157],
    "Sobel 3x3":    [4193.154, 2208.909, 1356.646, 1151.114],
    "Sharpen 3x3":  [2741.640, 1331.155, 756.692, 705.125],
    "Box Blur 5x5": [5043.961, 2721.109, 1565.686, 1456.987],
}

A8t_8192 = [5.90, 4.68, 5.09, 5.51]   # 8-thread speedup at 8192x8192
B8t_8192 = [3.31, 3.64, 3.89, 3.46]

ksizes = [3, 5, 7, 15]
A_k8 = [5.14, 5.61, 3.34, 6.43]       # kernel-sweep 8-thread speedup (4096x4096)
B_k8 = [3.42, 3.34, 3.41, 3.57]

ioA = [4648, 1621, 6269]              # PNG encode, decode, total (4096x4096)
ioB = [2257, 889, 3146]

CA, CB = "#1565c0", "#ef6c00"         # Machine A / Machine B colours
W = 0.38


def speedup(times):
    return [times[0] / t for t in times]


# 1) average speedup vs threads
plt.figure(figsize=(6, 4))
plt.plot(threads, avgA_sp, "o-", color=CA, label="Machine A (i5-8265U)")
plt.plot(threads, avgB_sp, "s-", color=CB, label="Machine B (i5-10300H)")
plt.plot(threads, threads, "k--", alpha=0.4, label="ideal linear")
plt.xlabel("threads"); plt.ylabel("average speedup S(n)")
plt.title("Average speedup vs thread count - Machine A vs B")
plt.legend(); plt.grid(True, alpha=0.3)
plt.savefig(f"{R}/cmp_avg_speedup.png", dpi=130, bbox_inches="tight"); plt.close()

# 2) average efficiency vs threads
plt.figure(figsize=(6, 4))
plt.plot(threads, avgA_ef, "o-", color=CA, label="Machine A")
plt.plot(threads, avgB_ef, "s-", color=CB, label="Machine B")
plt.axhline(1.0, color="k", ls="--", alpha=0.4, label="ideal (1.0)")
plt.xlabel("threads"); plt.ylabel("average efficiency E(n)")
plt.title("Average efficiency vs thread count - Machine A vs B")
plt.legend(); plt.grid(True, alpha=0.3); plt.ylim(0, 1.05)
plt.savefig(f"{R}/cmp_avg_efficiency.png", dpi=130, bbox_inches="tight"); plt.close()

# 3) 8192x8192 speedup vs threads, per filter (2x2 panels)
fig, axes = plt.subplots(2, 2, figsize=(9, 7))
for ax, f in zip(axes.flat, FILTERS):
    ax.plot(threads, speedup(A8192[f]), "o-", color=CA, label="Machine A")
    ax.plot(threads, speedup(B8192[f]), "s-", color=CB, label="Machine B")
    ax.plot(threads, threads, "k--", alpha=0.35)
    ax.set_title(f); ax.set_xlabel("threads"); ax.set_ylabel("speedup")
    ax.grid(True, alpha=0.3)
axes.flat[0].legend()
fig.suptitle("8192x8192 speedup vs threads, per filter - Machine A vs B")
fig.tight_layout(rect=[0, 0, 1, 0.97])
fig.savefig(f"{R}/cmp_8192_scaling.png", dpi=130, bbox_inches="tight"); plt.close(fig)

# 4) 8-thread speedup by filter at 8192x8192 (grouped bar)
x = np.arange(len(FILTERS))
plt.figure(figsize=(7, 4))
plt.bar(x - W/2, A8t_8192, W, color=CA, label="Machine A")
plt.bar(x + W/2, B8t_8192, W, color=CB, label="Machine B")
plt.xticks(x, FILTERS); plt.ylabel("8-thread speedup")
plt.title("8-thread speedup by filter (8192x8192) - Machine A vs B")
plt.legend(); plt.grid(True, axis="y", alpha=0.3)
plt.savefig(f"{R}/cmp_8t_filter_bar.png", dpi=130, bbox_inches="tight"); plt.close()

# 5) kernel-size sweep, 8-thread speedup (grouped bar)
x = np.arange(len(ksizes))
plt.figure(figsize=(7, 4))
plt.bar(x - W/2, A_k8, W, color=CA, label="Machine A")
plt.bar(x + W/2, B_k8, W, color=CB, label="Machine B")
plt.xticks(x, [f"{k}x{k}" for k in ksizes]); plt.ylabel("8-thread speedup")
plt.title("Kernel-size sweep, 8-thread speedup (Gaussian, 4096x4096) - A vs B")
plt.legend(); plt.grid(True, axis="y", alpha=0.3)
plt.savefig(f"{R}/cmp_kernel_sweep.png", dpi=130, bbox_inches="tight"); plt.close()

# 6) image I/O timing (grouped bar)
labels = ["PNG encode", "PNG decode", "total I/O"]
x = np.arange(len(labels))
plt.figure(figsize=(7, 4))
plt.bar(x - W/2, ioA, W, color=CA, label="Machine A")
plt.bar(x + W/2, ioB, W, color=CB, label="Machine B")
plt.xticks(x, labels); plt.ylabel("time (ms)")
plt.title("Image I/O timing at 4096x4096 - Machine A vs B")
plt.legend(); plt.grid(True, axis="y", alpha=0.3)
plt.savefig(f"{R}/cmp_io.png", dpi=130, bbox_inches="tight"); plt.close()

print("Two-machine comparison charts written to results/cmp_*.png")
