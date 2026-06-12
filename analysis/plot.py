import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import os

OUT = "results"
os.makedirs(OUT, exist_ok=True)
df = pd.read_csv(f"{OUT}/benchmark.csv")

threads_axis = sorted(df["threads"].unique())

for f in df["filter"].unique():
    sub = df[df["filter"] == f]
    # speedup vs threads, one line per size
    plt.figure()
    for s in sorted(sub["size"].unique()):
        d = sub[sub["size"] == s].sort_values("threads")
        plt.plot(d["threads"], d["speedup"], marker="o", label=f"{s}x{s}")
    plt.plot(threads_axis, threads_axis, "k--", alpha=0.4, label="ideal linear")
    plt.xlabel("threads"); plt.ylabel("speedup S(n)"); plt.title(f"Speedup - {f}")
    plt.legend(); plt.grid(True, alpha=0.3)
    plt.savefig(f"{OUT}/speedup_{f}.png", dpi=120, bbox_inches="tight"); plt.close()

    # efficiency vs threads
    plt.figure()
    for s in sorted(sub["size"].unique()):
        d = sub[sub["size"] == s].sort_values("threads")
        plt.plot(d["threads"], d["efficiency"], marker="o", label=f"{s}x{s}")
    plt.xlabel("threads"); plt.ylabel("efficiency E(n)"); plt.title(f"Efficiency - {f}")
    plt.legend(); plt.grid(True, alpha=0.3)
    plt.savefig(f"{OUT}/efficiency_{f}.png", dpi=120, bbox_inches="tight"); plt.close()

# speedup vs size at 8 threads, one line per filter
plt.figure()
top = df[df["threads"] == 8]
for f in top["filter"].unique():
    d = top[top["filter"] == f].sort_values("size")
    plt.plot(d["size"], d["speedup"], marker="o", label=f)
plt.xscale("log", base=2); plt.xlabel("image size (px per side)")
plt.ylabel("speedup S(8)"); plt.title("Speedup vs image size (8 threads)")
plt.legend(); plt.grid(True, alpha=0.3)
plt.savefig(f"{OUT}/speedup_vs_size.png", dpi=120, bbox_inches="tight"); plt.close()

# kernel sweep
ks = pd.read_csv(f"{OUT}/kernel_sweep.csv")
plt.figure()
for k in sorted(ks["k"].unique()):
    d = ks[ks["k"] == k].sort_values("threads")
    plt.plot(d["threads"], d["speedup"], marker="o", label=f"k={k}")
plt.plot(threads_axis, threads_axis, "k--", alpha=0.4, label="ideal linear")
plt.xlabel("threads"); plt.ylabel("speedup S(n)")
plt.title("Speedup vs kernel size (Gaussian, 4096x4096)")
plt.legend(); plt.grid(True, alpha=0.3)
plt.savefig(f"{OUT}/kernel_sweep.png", dpi=120, bbox_inches="tight"); plt.close()

print("Charts written to results/*.png")
