import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import {
  getFirestore,
  collection,
  onSnapshot,
  doc,
  updateDoc,
  Timestamp,
  addDoc,
  deleteDoc,
  setDoc,
} from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

// ================= KONFIGURASI FIREBASE RESMI LITERA =================
const firebaseConfig = {
  apiKey: "AIzaSyCKJjGFLpfV3itCslFKT1cb4Q000qFYV8Y",
  authDomain: "litera-ebc48.firebaseapp.com",
  projectId: "litera-ebc48",
  storageBucket: "litera-ebc48.firebasestorage.app",
  messagingSenderId: "958978788576",
  appId: "1:958978788576:web:e7a499f7ea15f46bf7d485",
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// Inisialisasi Grafik Variabel Global agar bisa di-update
let barChartInstance = null;
let pieChartInstance = null;

// ================= LOGIKA NAVIGASI SPA =================
window.switchPage = (pageId) => {
  // Sembunyikan semua halaman
  document.getElementById("page-dashboard").classList.add("hidden");
  document.getElementById("page-buku").classList.add("hidden");
  document.getElementById("page-mahasiswa").classList.add("hidden");
  document.getElementById("page-peminjaman").classList.add("hidden");

  // Hilangkan kelas active dari semua tombol sidebar
  document
    .querySelectorAll(".sidebar-link")
    .forEach((btn) => btn.classList.remove("active"));

  // Tampilkan halaman terpilih & set active ke tombolnya
  document.getElementById(`page-${pageId}`).classList.remove("hidden");
  document.getElementById(`menu-${pageId}`).classList.add("active");
};

// ================= BACA DATA REALTIME & GRAFIK (PEMINJAMAN) =================
onSnapshot(collection(db, "peminjaman"), (snapshot) => {
  let htmlTabel = "";
  let totalTahun = 0;
  let totalBulan = 0;
  let totalMenunggu = 0;

  // Objek tampungan penghitung grafik prodi & bulanan
  const prodiCount = {};
  const monthlyCount = Array(12).fill(0); // Jan-Des

  const sekarang = new Date();
  const tahunSekarang = sekarang.getFullYear();
  const bulanSekarang = sekarang.getMonth();

  snapshot.forEach((documentDoc) => {
    const data = documentDoc.data();
    const idDokumen = documentDoc.id;
    const totalBukuDalamDokumen = data.buku_dipinjam
      ? data.buku_dipinjam.length
      : 0;

    // 1. Filter Penghitung Ringkasan Card Atas
    if (data.status_peminjaman === "MENUNGGU_KONFIRMASI") totalMenunggu++;

    if (data.tanggal_pengajuan) {
      const tglData = data.tanggal_pengajuan.toDate();
      if (tglData.getFullYear() === tahunSekarang) {
        totalTahun += totalBukuDalamDokumen; // Total Buku 1 Tahun
        monthlyCount[tglData.getMonth()] += totalBukuDalamDokumen; // Data untuk Bar Chart

        if (tglData.getMonth() === bulanSekarang) {
          totalBulan += totalBukuDalamDokumen; // Total Buku 1 Bulan
        }
      }
    }

    // 2. Olah Data untuk Pie Chart (Perbandingan Prodi)
    if (
      data.jurusan &&
      (data.status_peminjaman === "DIPINJAM" ||
        data.status_peminjaman === "SELESAI")
    ) {
      prodiCount[data.jurusan] = (prodiCount[data.jurusan] || 0) + 1;
    }

    // 3. Render HTML Baris Tabel Peminjaman
    const tglPengajuanStr = data.tanggal_pengajuan
      ? data.tanggal_pengajuan.toDate().toLocaleString("id-ID")
      : "-";

    let daftarBukuHtml = "<ul class='list-disc pl-4 text-xs'>";
    if (data.buku_dipinjam && Array.isArray(data.buku_dipinjam)) {
      data.buku_dipinjam.forEach((buku) => {
        daftarBukuHtml += `<li><span class="font-semibold">${buku.judul}</span></li>`;
      });
    }
    daftarBukuHtml += "</ul>";

    let badgeColor = "bg-yellow-100 text-yellow-800";
    if (data.status_peminjaman === "DIPINJAM")
      badgeColor = "bg-blue-100 text-blue-800";
    if (data.status_peminjaman === "SELESAI")
      badgeColor = "bg-green-100 text-green-800";

    let tombolAksiHtml = "";
    if (
      data.status_peminjaman === "MENUNGGU_KONFIRFIRMASI" ||
      data.status_peminjaman === "MENUNGGU_KONFIRMASI"
    ) {
      tombolAksiHtml = `
                <button onclick="prosesAksi('${idDokumen}', 'DIPINJAM')" class="bg-green-500 hover:bg-green-600 text-white text-xs px-2 py-1 rounded font-medium shadow-sm transition">
                    Serahkan Buku
                </button>
            `;
    } else if (data.status_peminjaman === "DIPINJAM") {
      tombolAksiHtml = `
                <button onclick="prosesAksi('${idDokumen}', 'SELESAI')" class="bg-litera hover:bg-litera-dark text-white text-xs px-2 py-1 rounded font-medium shadow-sm transition">
                    Konfirmasi Kembali
                </button>
            `;
    } else {
      tombolAksiHtml = `<span class="text-gray-400 text-xs italic">Selesai</span>`;
    }

    htmlTabel += `
            <tr class="hover:bg-gray-50 transition text-sm">
                <td class="px-6 py-4">
                    <div class="font-bold text-gray-900">${data.name || "Anonim"}</div>
                    <div class="text-xs text-gray-500">${data.npm || "-"} - ${data.jurusan || "-"}</div>
                </td>
                <td class="px-6 py-4">${daftarBukuHtml}</td>
                <td class="px-6 py-4 text-gray-600">${tglPengajuanStr}</td>
                <td class="px-6 py-4">
                    <span class="px-2.5 py-0.5 text-xs font-semibold rounded-full ${badgeColor}">
                        ${data.status_peminjaman}
                    </span>
                </td>
                <td class="px-6 py-4 text-center">${tombolAksiHtml}</td>
            </tr>
        `;
  });

  // Suntikkan Angka ke Card Summary Atas
  document.getElementById("txt-total-tahun").innerText = `${totalTahun} Buku`;
  document.getElementById("txt-total-bulan").innerText = `${totalBulan} Buku`;
  document.getElementById("txt-total-konfirmasi").innerText =
    `${totalMenunggu} Request`;
  document.getElementById("tabel-peminjaman").innerHTML = htmlTabel;

  // Update Data Grafik secara Dinamis
  renderCharts(monthlyCount, prodiCount);
});

// ================= RENDER GRAFIK CHART.JS =================
function renderCharts(barData, pieData) {
  const ctxBar = document.getElementById("chartBatang").getContext("2d");
  if (barChartInstance) barChartInstance.destroy();
  barChartInstance = new Chart(ctxBar, {
    type: "bar",
    data: {
      labels: [
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "Mei",
        "Jun",
        "Jul",
        "Agu",
        "Sep",
        "Okt",
        "Nov",
        "Des",
      ],
      datasets: [
        {
          label: "Buku Dipinjam",
          data: barData,
          backgroundColor: "#EA6113",
          borderRadius: 5,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: { y: { beginAtZero: true } },
    },
  });

  const ctxPie = document.getElementById("chartDonat").getContext("2d");
  if (pieChartInstance) pieChartInstance.destroy();

  const pieLabels = Object.keys(pieData);
  const pieValues = Object.values(pieData);

  pieChartInstance = new Chart(ctxPie, {
    type: "doughnut",
    data: {
      labels: pieLabels.length ? pieLabels : ["Belum Ada Data"],
      datasets: [
        {
          data: pieValues.length ? pieValues : [1],
          backgroundColor: [
            "#EA6113",
            "#36A2EB",
            "#FFCE56",
            "#4BC0C0",
            "#9966FF",
          ],
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: "bottom" } },
    },
  });
}

// ================= LOGIKA UBAH STATUS PEMINJAMAN =================
window.prosesAksi = async (id, statusBaru) => {
  const docRef = doc(db, "peminjaman", id);
  try {
    if (statusBaru === "DIPINJAM") {
      await updateDoc(docRef, {
        status_peminjaman: statusBaru,
        tanggal_konfirmasi_admin: Timestamp.now(),
      });
      alert(
        "Buku Berhasil Diserahkan! Hitung mundur 1 hari aktif di mobile mahasiswa.",
      );
    } else if (statusBaru === "SELESAI") {
      await updateDoc(docRef, {
        status_peminjaman: statusBaru,
      });
      alert("Buku Berhasil Dikembalikan! Status peminjaman ditutup.");
    }
  } catch (error) {
    alert("Gagal memperbarui status: " + error.message);
  }
};

// ================= BACA DATA REALTIME KOLEKSI BUKU =================
const gridBuku = document.getElementById("grid-buku");
const badgeTotalBuku = document.getElementById("badge-total-buku");

onSnapshot(collection(db, "koleksi_buku"), (snapshot) => {
  let htmlGridBuku = "";
  let totalBuku = 0;

  snapshot.forEach((documentDoc) => {
    const data = documentDoc.data();
    totalBuku++;

    const idBuku = documentDoc.id;
    const judulBuku = data.judul || "Judul Tidak Diketahui";
    const penulisBuku = data.penulis || "Penulis Anonim";
    const kategoriBuku = data.kategori || "Umum";
    const coverBuku =
      data.imageUrl || "https://via.placeholder.com/150x210?text=No+Cover";

    htmlGridBuku += `
            <div class="relative rounded-xl border border-gray-200/50 overflow-hidden hover:shadow-lg transition-all duration-300 flex flex-col justify-between p-4 group" style="min-height: 350px;">
                <div class="absolute inset-0 z-0 bg-cover bg-center scale-110 blur-xl opacity-35 grayscale-[20%] group-hover:scale-125 transition-transform duration-500" style="background-image: url('${coverBuku}');"></div>
                <div class="absolute inset-0 z-0 bg-white/60 backdrop-blur-md"></div>
                <div class="relative z-10">
                    <div class="w-full aspect-[3/4] rounded-lg overflow-hidden bg-gray-100/50 mb-3 flex items-center justify-center shadow-md border border-black/5 relative">
                        <img src="${coverBuku}" class="w-full h-full object-cover" alt="Cover ${judulBuku}" onerror="this.src='https://via.placeholder.com/150x210?text=Litera'">
                        <div class="absolute inset-0 bg-black/40 flex items-center justify-center space-x-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                            <button onclick="prepareEditBuku('${idBuku}', '${btoa(encodeURIComponent(judulBuku))}', '${btoa(encodeURIComponent(penulisBuku))}', '${btoa(encodeURIComponent(kategoriBuku))}', '${coverBuku}')" class="bg-white/90 hover:bg-white text-blue-600 p-2 rounded-lg text-xs font-bold shadow transition flex items-center justify-center w-8 h-8">
                                <i class="fa-solid fa-pen"></i>
                            </button>
                            <button onclick="hapusBuku('${idBuku}', '${btoa(encodeURIComponent(judulBuku))}')" class="bg-red-500 hover:bg-red-600 text-white p-2 rounded-lg text-xs font-bold shadow transition flex items-center justify-center w-8 h-8">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </div>
                    <span class="text-[10px] uppercase tracking-wider font-extrabold bg-black/5 text-gray-700 px-2 py-0.5 rounded backdrop-blur-sm">
                        ${kategoriBuku}
                    </span>
                    <h4 class="font-bold text-sm text-gray-900 mt-2 line-clamp-2 drop-shadow-sm" title="${judulBuku}">${judulBuku}</h4>
                    <p class="text-xs text-gray-600 mt-0.5 truncate font-medium">${penulisBuku}</p>
                </div>
                <div class="relative z-10 border-t border-black/5 pt-3 mt-3 flex items-center justify-between text-[11px] text-gray-500 font-medium">
                    <span>ID: ${idBuku.substring(0, 6)}...</span>
                    <span class="text-emerald-700 flex items-center">
                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 mr-1.5 animate-pulse"></span> Ready
                    </span>
                </div>
            </div>
        `;
  });

  if (gridBuku) gridBuku.innerHTML = htmlGridBuku;
  if (badgeTotalBuku) badgeTotalBuku.innerText = `${totalBuku} Judul Buku`;
});

// ================= LOGIKA INTERAKSI MODAL & PROSES CRUD BUKU =================
const modalBuku = document.getElementById("modal-buku");
const formBuku = document.getElementById("form-buku");

window.openBukuModal = () => {
  if (formBuku) formBuku.reset();
  document.getElementById("input-buku-id").value = "";
  document.getElementById("modal-buku-title").innerText = "Tambah Koleksi Buku";
  modalBuku.classList.remove("hidden");
  setTimeout(() => modalBuku.classList.remove("opacity-0"), 10);
};

window.closeBukuModal = () => {
  modalBuku.classList.add("opacity-0");
  setTimeout(() => modalBuku.classList.add("hidden"), 300);
};

window.prepareEditBuku = (id, judulB64, penulisB64, kategoriB64, url) => {
  document.getElementById("input-buku-id").value = id;
  document.getElementById("input-buku-judul").value = decodeURIComponent(
    atob(judulB64),
  );
  document.getElementById("input-buku-penulis").value = decodeURIComponent(
    atob(penulisB64),
  );
  document.getElementById("input-buku-kategori").value = decodeURIComponent(
    atob(kategoriB64),
  );
  document.getElementById("input-buku-url").value = url;

  document.getElementById("modal-buku-title").innerText = "Edit Data Buku";
  modalBuku.classList.remove("hidden");
  setTimeout(() => modalBuku.classList.remove("opacity-0"), 10);
};

if (formBuku) {
  formBuku.addEventListener("submit", async (e) => {
    e.preventDefault();
    const idBuku = document.getElementById("input-buku-id").value;
    const dataBuku = {
      judul: document.getElementById("input-buku-judul").value,
      penulis: document.getElementById("input-buku-penulis").value,
      kategori: document.getElementById("input-buku-kategori").value,
      imageUrl: document.getElementById("input-buku-url").value,
    };
    try {
      if (idBuku) {
        await setDoc(doc(db, "koleksi_buku", idBuku), dataBuku, {
          merge: true,
        });
        alert("Data buku berhasil diperbarui!");
      } else {
        await addDoc(collection(db, "koleksi_buku"), dataBuku);
        alert("Buku baru berhasil ditambahkan!");
      }
      closeBukuModal();
    } catch (error) {
      alert("Gagal memproses data buku: " + error.message);
    }
  });
}

window.hapusBuku = async (id, judulB64) => {
  const judul = decodeURIComponent(atob(judulB64));
  if (
    confirm(
      `Apakah kamu yakin ingin menghapus buku "${judul}" dari perpustakaan?`,
    )
  ) {
    try {
      await deleteDoc(doc(db, "koleksi_buku", id));
      alert("Buku berhasil dihapus dari sistem.");
    } catch (error) {
      alert("Gagal menghapus buku: " + error.message);
    }
  }
};

// ================= BACA DATA REALTIME KOLEKSI USERS (CRUD MAHASISWA BARU - FIXED) =================
const tabelMahasiswa = document.getElementById("tabel-mahasiswa");
const badgeTotalMahasiswa = document.getElementById("badge-total-mahasiswa");
const modalMhs = document.getElementById("modal-mhs");
const formMahasiswa = document.getElementById("form-mahasiswa");

onSnapshot(collection(db, "users"), (snapshot) => {
  let htmlTabelMhs = "";
  let totalMhs = 0;

  snapshot.forEach((documentDoc) => {
    const data = documentDoc.data();
    totalMhs++;

    const npmMhs = documentDoc.id;
    const namaMhs = data.nama || "Nama Belum Diisi";
    const fakultasMhs = data.fakultas || "-";
    const prodiMhs = data.jurusan || "-";

    const b64Nama = btoa(encodeURIComponent(namaMhs));
    const b64Fakultas = btoa(encodeURIComponent(fakultasMhs));
    const b64Prodi = btoa(encodeURIComponent(prodiMhs));

    htmlTabelMhs += `
            <tr class="hover:bg-gray-50 transition text-sm">
                <td class="px-6 py-4 flex items-center space-x-3">
                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(namaMhs)}&background=random&color=fff" class="w-8 h-8 rounded-full" alt="Avatar">
                    <div class="font-bold text-gray-900">${namaMhs}</div>
                </td>
                <td class="px-6 py-4 font-mono text-gray-600 font-semibold">${npmMhs}</td>
                <td class="px-6 py-4 text-gray-700">${fakultasMhs}</td>
                <td class="px-6 py-4 text-gray-700">${prodiMhs}</td>
                <td class="px-6 py-4">
                    <span class="px-2 py-0.5 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                        ✔ Aktif
                    </span>
                </td>
                <td class="px-6 py-4 text-center space-x-1.5 whitespace-nowrap">
                    <button onclick="prepareEditMhs('${npmMhs}', '${b64Nama}', '${b64Fakultas}', '${b64Prodi}')" class="text-blue-600 hover:text-blue-800 font-bold text-xs bg-blue-50 hover:bg-blue-100 px-2.5 py-1 rounded-md transition-all">
                        <i class="fa-solid fa-pen mr-1"></i>Edit
                    </button>
                    <button onclick="hapusMhs('${npmMhs}', '${b64Nama}')" class="text-red-600 hover:text-red-800 font-bold text-xs bg-red-50 hover:bg-red-100 px-2.5 py-1 rounded-md transition-all">
                        <i class="fa-solid fa-trash mr-1"></i>Hapus
                    </button>
                </td>
            </tr>
        `;
  });

  if (tabelMahasiswa) {
    tabelMahasiswa.innerHTML =
      htmlTabelMhs ||
      `<tr><td colspan="6" class="px-6 py-4 text-center text-gray-400 italic">Belum ada mahasiswa terdaftar</td></tr>`;
  }
  if (badgeTotalMahasiswa) {
    badgeTotalMahasiswa.innerText = `${totalMhs} Mahasiswa`;
  }
});

// ================= LOGIKA INTERAKSI MODAL & PROSES CRUD MAHASISWA =================
window.openMhsModal = () => {
  if (formMahasiswa) formMahasiswa.reset();
  document.getElementById("input-mhs-is-edit").value = "false";

  const npmInput = document.getElementById("input-mhs-npm");
  npmInput.disabled = false;
  npmInput.classList.remove(
    "bg-gray-100",
    "cursor-not-allowed",
    "text-gray-400",
  );

  document.getElementById("modal-mhs-title").innerText =
    "Tambah Mahasiswa Baru";
  modalMhs.classList.remove("hidden");
  setTimeout(() => modalMhs.classList.remove("opacity-0"), 10);
};

window.closeMhsModal = () => {
  modalMhs.classList.add("opacity-0");
  setTimeout(() => modalMhs.classList.add("hidden"), 300);
};

window.prepareEditMhs = (npm, namaB64, fakultasB64, prodiB64) => {
  document.getElementById("input-mhs-is-edit").value = "true";

  document.getElementById("input-mhs-npm").value = npm;
  document.getElementById("input-mhs-nama").value = decodeURIComponent(
    atob(namaB64),
  );
  document.getElementById("input-mhs-fakultas").value = decodeURIComponent(
    atob(fakultasB64),
  );
  document.getElementById("input-mhs-jurusan").value = decodeURIComponent(
    atob(prodiB64),
  );

  const npmInput = document.getElementById("input-mhs-npm");
  npmInput.disabled = true;
  npmInput.classList.add("bg-gray-100", "cursor-not-allowed", "text-gray-400");

  document.getElementById("modal-mhs-title").innerText = "Edit Data Mahasiswa";
  modalMhs.classList.remove("hidden");
  setTimeout(() => modalMhs.classList.remove("opacity-0"), 10);
};

if (formMahasiswa) {
  formMahasiswa.addEventListener("submit", async (e) => {
    e.preventDefault();

    const npmId = document.getElementById("input-mhs-npm").value.trim();
    const isEditMode =
      document.getElementById("input-mhs-is-edit").value === "true";

    const dataMahasiswa = {
      nama: document.getElementById("input-mhs-nama").value,
      fakultas: document.getElementById("input-mhs-fakultas").value,
      jurusan: document.getElementById("input-mhs-jurusan").value,
    };

    try {
      const docRef = doc(db, "users", npmId);
      if (isEditMode) {
        await setDoc(docRef, dataMahasiswa, { merge: true });
        alert("Data mahasiswa berhasil diperbarui!");
      } else {
        await setDoc(docRef, dataMahasiswa);
        alert("Mahasiswa baru berhasil ditambahkan!");
      }
      closeMhsModal();
    } catch (error) {
      alert("Gagal memproses data mahasiswa: " + error.message);
    }
  });
}

window.hapusMhs = async (npm, namaB64) => {
  const nama = decodeURIComponent(atob(namaB64));
  if (
    confirm(
      `Apakah kamu yakin ingin menghapus data mahasiswa "${nama}" (${npm})?`,
    )
  ) {
    try {
      await deleteDoc(doc(db, "users", npm));
      alert("Data mahasiswa berhasil dihapus dari sistem.");
    } catch (error) {
      alert("Gagal menghapus data: " + error.message);
    }
  }
};
