let stagedGifts = [];

function uploadPhoto(input) {
    const file = input.files[0];
    if (!file) return;

    const spinner = document.getElementById('uploadSpinner');
    const successText = document.getElementById('uploadSuccess');
    const btnAdd = document.getElementById('btnAddToList');

    spinner.classList.remove('d-none');
    successText.classList.add('d-none');
    btnAdd.disabled = true;

    const formData = new FormData();
    formData.append('file', file);

    fetch('/api/gifts/photo', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error('Falha no upload');
        return response.text();
    })
    .then(photoUrl => {
        document.getElementById('giftPhotoUrl').value = photoUrl;
        spinner.classList.add('d-none');
        successText.classList.remove('d-none');
        btnAdd.disabled = false;
    })
    .catch(error => {
        spinner.classList.add('d-none');
        alert('Erro ao fazer upload: ' + error.message);
    });
}

function addGiftToList(event) {
    event.preventDefault();
    const name = document.getElementById('giftName').value;
    const price = document.getElementById('giftPrice').value;
    const description = document.getElementById('giftDescription').value;
    const photoUrl = document.getElementById('giftPhotoUrl').value;

    const gift = { name, price: parseFloat(price), description, photoUrl };
    stagedGifts.push(gift);

    document.getElementById('giftForm').reset();
    document.getElementById('uploadSuccess').classList.add('d-none');
    document.getElementById('btnAddToList').disabled = true;
    renderTable();
}

function removeGiftFromList(index) {
    stagedGifts.splice(index, 1);
    renderTable();
}

function renderTable() {
    const tbody = document.getElementById('giftsTableBody');
    if(!tbody) return; // Proteção para telas que não têm a tabela

    tbody.innerHTML = '';
    if (stagedGifts.length === 0) {
        tbody.innerHTML = `<tr id="emptyRow"><td colspan="5" class="text-center text-muted py-4">Nenhum presente adicionado ao lote ainda.</td></tr>`;
        document.getElementById('btnSaveBatch').disabled = true;
        document.getElementById('batchCount').innerText = '0';
        return;
    }

    stagedGifts.forEach((gift, index) => {
        const row = `
            <tr>
                <td><img src="${gift.photoUrl}" class="preview-img" alt="Preview" style="width:50px; height:50px; object-fit:cover;"></td>
                <td><strong>${gift.name}</strong></td>
                <td>R$ ${gift.price.toFixed(2)}</td>
                <td class="text-truncate" style="max-width: 200px;">${gift.description || '-'}</td>
                <td class="text-end">
                    <button class="btn btn-outline-danger btn-sm" onclick="removeGiftFromList(${index})">✕ Retirar</button>
                </td>
            </tr>`;
        tbody.innerHTML += row;
    });

    document.getElementById('btnSaveBatch').disabled = false;
    document.getElementById('batchCount').innerText = stagedGifts.length;
}

function saveGiftsBatch() {
    const btnSave = document.getElementById('btnSaveBatch');
    const batchCountSpan = document.getElementById('batchCount');

    btnSave.disabled = true;
    batchCountSpan.innerText = '⏳';

    fetch('/api/gifts/batch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(stagedGifts)
    })
    .then(async response => {
        if (!response.ok) throw new Error('Falha no servidor.');
        return response.json();
    })
    .then(data => {
        alert(`Sucesso! ${data.length} presentes salvos.`);
        stagedGifts = [];
        renderTable();
    })
    .catch(error => {
        alert('Aviso: ' + error.message);
        btnSave.disabled = false;
        renderTable();
    });
}

function uploadCsvGuests() {
    const fileInput = document.getElementById('csvFile');
    const file = fileInput.files[0];
    if (!file) {
        showAlert('Selecione um arquivo.', 'alert-danger');
        return;
    }
    const formData = new FormData();
    formData.append('file', file);
    showAlert('Processando...', 'alert-info');

    fetch('/api/guests/upload-csv', {
        method: 'POST',
        body: formData
    })
    .then(async response => {
        const text = await response.text();
        if (!response.ok) throw new Error(text || 'Falha no upload.');
        return text;
    })
    .then(message => {
        showAlert('🎉 ' + message, 'alert-success');
        fileInput.value = '';
    })
    .catch(error => showAlert('Erro: ' + error.message, 'alert-danger'));
}

function showAlert(message, bootstrapClass) {
    const alertBox = document.getElementById('csvAlert');
    if(!alertBox) return;
    alertBox.className = `alert ${bootstrapClass} mt-3`;
    alertBox.innerText = message;
    alertBox.classList.remove('d-none');
}

// ==========================================
// MÓDULO DE PAGAMENTOS (TESTES)
// ==========================================

function gerarPix(giftId, btnElement) {
    const textoOriginal = btnElement.innerHTML;
    btnElement.innerHTML = '⏳ Aguarde...';
    btnElement.disabled = true;

    fetch(`/api/payments/pix/${giftId}`, {
        method: 'POST'
    })
    .then(async response => {
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Erro crítico ao gerar o Pix.');
        return data;
    })
    .then(data => {
        document.getElementById('pixQrCodeImg').src = 'data:image/png;base64,' + data.qrCodeBase64;
        document.getElementById('pixCopiaCola').value = data.qrCodeCopiaECola;
        const pixModalElement = document.getElementById('pixModal');
        const pixModal = new bootstrap.Modal(pixModalElement);
        pixModal.show();
    })
    .catch(error => {
        alert('Falha na comunicação com o Mercado Pago: ' + error.message);
    })
    .finally(() => {
        btnElement.innerHTML = textoOriginal;
        btnElement.disabled = false;
    });
}

function copiarPix() {
    const inputPix = document.getElementById('pixCopiaCola');
    inputPix.select();
    inputPix.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(inputPix.value);
    alert("Copiado com sucesso!");
}